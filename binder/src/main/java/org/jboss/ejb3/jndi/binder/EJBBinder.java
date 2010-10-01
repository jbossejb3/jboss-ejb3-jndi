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
package org.jboss.ejb3.jndi.binder;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.ejb3.jndi.binder.impl.View;
import org.jboss.ejb3.jndi.binder.metadata.SessionBeanType;
import org.jboss.ejb3.jndi.binder.spi.ProxyFactory;
import org.jboss.logging.Logger;
import org.jboss.reloaded.naming.spi.JavaEEApplication;
import org.jboss.reloaded.naming.spi.JavaEEModule;
import org.jboss.util.naming.Util;

/**
 * Bind a view into JNDI according to EJB 3.1 4.4 Global JNDI Access.
 *
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class EJBBinder
{
   private static final Logger log = Logger.getLogger(EJBBinder.class);
   
   private SessionBeanType bean;
   private Context globalContext;
   private ProxyFactory proxyFactory;

   private Collection<View> views = new LinkedList<View>();

   public EJBBinder(SessionBeanType bean)
   {
      this.bean = bean;

      constructViews(views, bean.getBusinessLocals(), View.Type.BUSINESS_LOCAL, bean);
      constructViews(views, bean.getBusinessRemotes(), View.Type.BUSINESS_REMOTE, bean);
      constructView(views, bean.getHome(), View.Type.HOME, bean);
      constructView(views, bean.getLocalHome(), View.Type.LOCAL_HOME, bean);

      if(bean.isLocalBean())
         views.add(new View(bean.getEJBClass(), View.Type.LOCAL_BEAN, bean));
   }

   private static void constructViews(Collection<View> views, Collection<Class<?>> businessInterfaces, View.Type type, SessionBeanType bean)
   {
      if(businessInterfaces == null)
         return;

      for(Class<?> businessInterface : businessInterfaces)
      {
         views.add(new View(businessInterface, type, bean));
      }
   }
   
   private static void constructView(Collection<View> views, Class<?> businessInterface, View.Type type, SessionBeanType bean)
   {
      if(businessInterface == null)
         return;
      Collection<Class<?>> businessInterfaces = new HashSet<Class<?>>();
      businessInterfaces.add(businessInterface);
      constructViews(views, businessInterfaces, type, bean);
   }

   // PostConstruct
   public void bind() throws NamingException
   {
      for(View view : views)
      {
         Object proxy = proxyFactory.produce(view);
         // 4.4.1
         bindGlobal(view, proxy);
         // 4.4.1.1
         bindApp(view, proxy);
         // 4.4.1.2
         bindModule(view, proxy);
      }
   }

   protected void bind(Context ctx, String name, Object obj) throws NamingException
   {
      if(log.isDebugEnabled())
         log.debug("Bound " + obj + " at " + name + " under " + ctx);
      Util.bind(ctx, name, obj);
   }

   protected void bindApp(View view, Object proxy) throws NamingException
   {
      // TODO: single view has no interface-name
      String name = getAppJNDIName(view.getBusinessInterface());
      bind(bean.getModule().getApplication().getContext(), name, proxy);
   }

   protected void bindGlobal(View view, Object proxy) throws NamingException
   {
      // TODO: single view has no interface-name
      bind(globalContext, getGlobalJNDIName(view.getBusinessInterface()), proxy);
   }

   protected void bindModule(View view, Object proxy) throws NamingException
   {
      // TODO: single view has no interface-name
      bind(bean.getModule().getContext(), getModuleJNDIName(view.getBusinessInterface()), proxy);
   }

   /**
    * @return the name within the app name space
    */
   protected String getAppJNDIName(Class<?> businessInterface)
   {
      JavaEEModule module = bean.getModule();
      String name = module.getName() + "/" + getModuleJNDIName(businessInterface);
      return name;
   }

   /**
    * @return the name within the global name space.
    */
   protected String getGlobalJNDIName(Class<?> businessInterface)
   {
      JavaEEModule module = bean.getModule();
      JavaEEApplication app = module.getApplication();
      // EJB 3.1 4.4.1 <app-name> only applies if the session bean is packaged within an .ear file.
      String appName = app.isEnterpriseApplicationArchive() ? app.getName() : null;
      String name = (appName != null ? appName + "/" : "") + getAppJNDIName(businessInterface);
      return name;
   }

   /**
    * @return the name within the module name space.
    */
   protected String getModuleJNDIName(Class<?> businessInterface)
   {
      String name = bean.getName() + (businessInterface != null ? "!" + businessInterface.getName() : "");
      return name;
   }

   public void setGlobalContext(Context context)
   {
      this.globalContext = context;
   }

   public void setProxyFactory(ProxyFactory proxyFactory)
   {
      this.proxyFactory = proxyFactory;
   }
   
   // PreDestroy
   public void unbind() throws NamingException
   {
      for(View view : views)
      {
         unbindModule(view);
         unbindApp(view);
         unbindGlobal(view);
      }
   }

   protected void unbindApp(View view) throws NamingException
   {
      bean.getModule().getApplication().getContext().unbind(getAppJNDIName(view.getBusinessInterface()));
   }

   protected void unbindGlobal(View view) throws NamingException
   {
      globalContext.unbind(getGlobalJNDIName(view.getBusinessInterface()));
   }

   protected void unbindModule(View view) throws NamingException
   {
      bean.getModule().getContext().unbind(getModuleJNDIName(view.getBusinessInterface()));
   }
}
