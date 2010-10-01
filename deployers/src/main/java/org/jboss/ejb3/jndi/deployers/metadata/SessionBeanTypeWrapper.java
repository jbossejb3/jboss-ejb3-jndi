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
package org.jboss.ejb3.jndi.deployers.metadata;

import java.util.Collection;
import java.util.LinkedList;

import javax.naming.Context;

import org.jboss.ejb3.jndi.binder.metadata.SessionBeanType;
import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.reloaded.naming.spi.JavaEEComponent;
import org.jboss.reloaded.naming.spi.JavaEEModule;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class SessionBeanTypeWrapper implements SessionBeanType
{
   private JBossSessionBeanMetaData sessionBeanMetaData;
   private ClassLoader classLoader;

   private Collection<Class<?>> businessLocals;
   private Collection<Class<?>> businessRemotes;
   private Class<?> home;
   private Class<?> localHome;
   private Class<?> ejbClass;
   private boolean isLocalBean;
   private JavaEEComponent delegate;

   public SessionBeanTypeWrapper(JBossSessionBeanMetaData sessionBeanMetaData, ClassLoader classLoader, JavaEEComponent delegate)
           throws ClassNotFoundException
   {
      this.sessionBeanMetaData = sessionBeanMetaData;
      this.classLoader = classLoader;
      this.delegate = delegate;

      this.businessLocals = convert(sessionBeanMetaData.getBusinessLocals(), classLoader);
      this.businessRemotes = convert(sessionBeanMetaData.getBusinessRemotes(), classLoader);
      this.home = convert(sessionBeanMetaData.getHome(), classLoader);
      this.localHome = convert(sessionBeanMetaData.getLocalHome(), classLoader);
      this.ejbClass = Class.forName(sessionBeanMetaData.getEjbClass(), true, classLoader);
      if (sessionBeanMetaData.getEjbJarMetaData().isEJB31() && sessionBeanMetaData instanceof JBossSessionBean31MetaData)
      {
         this.isLocalBean = ((JBossSessionBean31MetaData) sessionBeanMetaData).isNoInterfaceBean();
      }
   }

   private static Collection<Class<?>> convert(Collection<String> classNames, ClassLoader loader)
           throws ClassNotFoundException
   {
      if(classNames == null)
         return null;

      Collection<Class<?>> classes = new LinkedList<Class<?>>();
      for(String className : classNames)
      {
         classes.add(Class.forName(className, true, loader));
      }
      return classes;
   }
   
   private static Class<?> convert(String className, ClassLoader loader) throws ClassNotFoundException
   {
      if (className == null)
         return null;

      return Class.forName(className, false, loader);
   }

   @Override
   public Collection<Class<?>> getBusinessLocals()
   {
      return businessLocals;
   }

   @Override
   public Collection<Class<?>> getBusinessRemotes()
   {
      return businessRemotes;
   }

   @Override
   public Class<?> getEJBClass()
   {
      return ejbClass;
   }

   @Override
   public Class<?> getHome()
   {
      return this.home;
   }

   @Override
   public Class<?> getRemote()
   {
      throw new RuntimeException("NYI: org.jboss.ejb3.jndi.deployers.metadata.SessionBeanTypeWrapper.getRemote");
   }

   @Override
   public Class<?> getLocal()
   {
      throw new RuntimeException("NYI: org.jboss.ejb3.jndi.deployers.metadata.SessionBeanTypeWrapper.getLocal");
   }

   @Override
   public Class<?> getLocalHome()
   {
      return this.localHome;
   }

   public JBossSessionBeanMetaData getSessionBeanMetaData()
   {
      return sessionBeanMetaData;
   }

   @Override
   public boolean isLocalBean()
   {
      return isLocalBean;
   }

   @Override
   public Context getContext()
   {
      return delegate.getContext();
   }

   @Override
   public JavaEEModule getModule()
   {
      return delegate.getModule();
   }

   @Override
   public String getName()
   {
      return delegate.getName();
   }
}
