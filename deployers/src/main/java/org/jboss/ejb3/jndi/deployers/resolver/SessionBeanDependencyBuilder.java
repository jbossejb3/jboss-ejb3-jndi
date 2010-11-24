/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
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

package org.jboss.ejb3.jndi.deployers.resolver;

import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.SessionType;
import org.jboss.reloaded.naming.deployers.javaee.JavaEEComponentInformer;

/**
 * Session bean dependency builder.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SessionBeanDependencyBuilder implements DependencyBuilder
{
   static final String SINGLETON = "jboss-ejb3-singleton-jndi-binder:";
   static final String NO_INTERFACE = "jboss-ejb3-nointerface-jndi-binder:";

   private SessionBeanDependencyHandler singletonHandler;
   private SessionBeanDependencyHandler sessionBeansHandler;
   private SessionBeanDependencyHandler noInterfaceHandler;

   private JavaEEComponentInformer informer;

   public SessionBeanDependencyBuilder(JavaEEComponentInformer informer)
   {
      if (informer == null)
         throw new IllegalArgumentException("Null informer");
      this.informer = informer;
   }

   public void buildDependency(DeploymentUnit unit, BeanMetaDataBuilder builder)
   {
      JBossSessionBeanMetaData jsbmd = unit.getAttachment(JBossSessionBeanMetaData.class);
      if (jsbmd != null)
      {
         if (jsbmd.isSession() && jsbmd.getSessionType() == SessionType.Singleton)
            handleSingleton(unit, builder, jsbmd);
         else if (jsbmd.isStateless() || jsbmd.isStateless())
            handleSessionBean(unit, builder, jsbmd);

         if (isNoInterface(jsbmd))
            handleNoInterfaceView(unit, builder, jsbmd);
      }
   }

   protected boolean isNoInterface(JBossSessionBeanMetaData jsbmd)
   {
      if (jsbmd.isSession() && jsbmd instanceof JBossSessionBean31MetaData)
      {
         JBossSessionBean31MetaData sessionBeanMetaData = (JBossSessionBean31MetaData) jsbmd;
         return sessionBeanMetaData.isNoInterfaceBean();
      }
      return false;
   }

   protected Object getName(String prefix, DeploymentUnit unit, String bean)
   {
      StringBuilder sb = new StringBuilder(prefix);
      String appName = informer.getApplicationName(unit);
      if (appName != null)
         sb.append("application=").append(appName).append(",");
      sb.append("module=").append(informer.getModuleName(unit)).append(",");
      sb.append("bean=").append(bean);
      return sb.toString();
   }

   protected void handleSingleton(DeploymentUnit unit, BeanMetaDataBuilder builder, JBossSessionBeanMetaData jsbmd)
   {
      if (singletonHandler != null)
         singletonHandler.handle(unit, builder, jsbmd);
      else
      {
         builder.addDemand(getName(SINGLETON, unit, jsbmd.getEjbName()), ControllerState.START, null);
      }
   }

   protected void handleSessionBean(DeploymentUnit unit, BeanMetaDataBuilder builder, JBossSessionBeanMetaData jsbmd)
   {
      if (sessionBeansHandler != null)
         sessionBeansHandler.handle(unit, builder, jsbmd);
      else
      {
         builder.addDemand(jsbmd.getContainerName(), ControllerState.START, null);
      }
   }

   protected void handleNoInterfaceView(DeploymentUnit unit, BeanMetaDataBuilder builder, JBossSessionBeanMetaData jsbmd)
   {
      if (noInterfaceHandler != null)
         noInterfaceHandler.handle(unit, builder, jsbmd);
      else
      {
         builder.addDemand(getName(NO_INTERFACE, unit, jsbmd.getEjbName()), ControllerState.START, null);
      }
   }

   public void setSingletonHandler(SessionBeanDependencyHandler singletonHandler)
   {
      this.singletonHandler = singletonHandler;
   }

   public void setSessionBeansHandler(SessionBeanDependencyHandler sessionBeansHandler)
   {
      this.sessionBeansHandler = sessionBeansHandler;
   }

   public void setNoInterfaceHandler(SessionBeanDependencyHandler noInterfaceHandler)
   {
      this.noInterfaceHandler = noInterfaceHandler;
   }
}
