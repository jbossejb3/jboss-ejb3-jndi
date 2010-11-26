/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.jndi.deployers.proxy;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;

import org.jboss.ejb3.jndi.binder.impl.View;
import org.jboss.ejb3.jndi.binder.spi.ProxyFactory;
import org.jboss.ejb3.jndi.deployers.metadata.SessionBeanTypeWrapper;
import org.jboss.metadata.ejb.jboss.jndi.resolver.impl.JNDIPolicyBasedSessionBean31JNDINameResolver;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

/**
 * Lazy proxy factory.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class LazyProxyFactory implements ProxyFactory
{
   private JNDIPolicyBasedSessionBean31JNDINameResolver nameResolver = new JNDIPolicyBasedSessionBean31JNDINameResolver();

   @Override
   public Object produce(View view)
   {
      SessionBeanTypeWrapper beanType = (SessionBeanTypeWrapper) view.getMetadata();
      String className = view.getBusinessInterface().getName();
      String linkName = nameResolver.resolveJNDIName(beanType.getSessionBeanMetaData(), className);
      // Setup the reference
      String factory = LazyObjectFactory.class.getName();
      RefAddr addr = new StringRefAddr("link", linkName);
      return new Reference(className, addr, factory, null);
   }

   private static class LazyObjectFactory implements ObjectFactory
   {
      public Object getObjectInstance(Object obj, Name name, Context context, Hashtable<?, ?> hashtable) throws Exception
      {
         if (obj == null || obj instanceof Reference == false)
            return null;

         Reference ref = (Reference) obj;
         RefAddr addr = ref.get("link");
         if (addr == null || addr instanceof StringRefAddr == false)
            return null;

         String link = (String) addr.getContent();
         ClassLoader tccl = Thread.currentThread().getContextClassLoader(); // HACK?
         Class<?> clazz = tccl.loadClass(ref.getClassName());

         javassist.util.proxy.ProxyFactory factory = new javassist.util.proxy.ProxyFactory();
         factory.setFilter(FINALIZE_FILTER);
         if (clazz.isInterface())
            factory.setInterfaces(new Class[]{clazz});
         else
            factory.setSuperclass(clazz);
         Class<?> proxyClass = getProxyClass(factory);
         ProxyObject proxy = (ProxyObject)proxyClass.newInstance();
         proxy.setHandler(new LazyHandler(link, context));
         return proxy;
      }
   }

   protected static Class<?> getProxyClass(javassist.util.proxy.ProxyFactory factory)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
         return factory.createClass();
      else
         return AccessController.doPrivileged(new ClassCreator(factory));
   }

   private static final MethodFilter FINALIZE_FILTER = new MethodFilter()
   {
      public boolean isHandled(Method m)
      {
         // skip finalize methods
         return !("finalize".equals(m.getName()) && m.getParameterTypes().length == 0);
      }
   };

   /**
    * Lazy method handler.
    */
   public static class LazyHandler implements MethodHandler
   {
      private String link;
      private Context context;
      private Object target;

      public LazyHandler(String link, Context context)
      {
         this.link = link;
         this.context = context;
      }

      public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable
      {
         if (target == null)
            target = context.lookup(link);

         return thisMethod.invoke(target, args);
      }
   }

   /**
    * Privileged class creator.
    */
   protected static class ClassCreator implements PrivilegedAction<Class<?>>
   {
      private javassist.util.proxy.ProxyFactory factory;

      public ClassCreator(javassist.util.proxy.ProxyFactory factory)
      {
         this.factory = factory;
      }

      public Class<?> run()
      {
         return factory.createClass();
      }
   }
}
