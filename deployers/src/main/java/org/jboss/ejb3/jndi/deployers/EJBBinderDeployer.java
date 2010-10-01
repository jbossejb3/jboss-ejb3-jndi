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
package org.jboss.ejb3.jndi.deployers;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.jndi.binder.EJBBinder;
import org.jboss.ejb3.jndi.binder.metadata.SessionBeanType;
import org.jboss.ejb3.jndi.deployers.metadata.SessionBeanTypeWrapper;
import org.jboss.ejb3.jndi.deployers.proxy.LegacyProxyFactory;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.reloaded.naming.deployers.javaee.JavaEEComponentInformer;
import org.jboss.reloaded.naming.spi.JavaEEComponent;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class EJBBinderDeployer extends AbstractJavaEEComponentDeployer
{
   public EJBBinderDeployer(JavaEEComponentInformer informer)
   {
      super(informer);
      setInput(JBossEnterpriseBeanMetaData.class);
      setOutput(BeanMetaData.class);
   }

   @Override
   protected void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      // We only work for EJB 3.x session beans
      JBossEnterpriseBeanMetaData beanMetaData = unit.getAttachment(JBossEnterpriseBeanMetaData.class);
      if(beanMetaData == null)
         return;

      if(!beanMetaData.isSession())
         return;

      if(!beanMetaData.getEjbJarMetaData().isEJB3x())
         return;

      JBossSessionBeanMetaData sessionBeanMetaData = (JBossSessionBeanMetaData) beanMetaData;

      String appName = getApplicationName(unit);
      String moduleName = getModuleName(unit);
      String componentName = getComponentName(unit);

      String javaCompName = "jboss.naming:";
      if(appName != null)
         javaCompName += "application=" + appName + ",";
      javaCompName += "module=" + moduleName + ",component=" + componentName;

      String sessionBeanTypeName = "jboss.ejb3:";
      if(appName != null)
         sessionBeanTypeName += "application=" + appName + ",";
      sessionBeanTypeName += "module=" + moduleName + ",component=" + componentName + ",service=" + SessionBeanTypeWrapper.class.getSimpleName();
      {
         BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(sessionBeanTypeName, SessionBeanTypeWrapper.class.getName());
         builder.addConstructorParameter(JBossSessionBeanMetaData.class.getName(), sessionBeanMetaData);
         builder.addConstructorParameter(ClassLoader.class.getName(), unit.getClassLoader());
         builder.addConstructorParameter(JavaEEComponent.class.getName(), builder.createInject(javaCompName));

         unit.getParent().addAttachment(sessionBeanTypeName, builder.getBeanMetaData());
      }

      {
         String beanInstanceName = "jboss.ejb3:";
         if(appName != null)
            beanInstanceName += "application=" + appName + ",";
         beanInstanceName += "module=" + moduleName + ",component=" + componentName + ",service=" + EJBBinder.class.getSimpleName();
         BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(beanInstanceName, EJBBinder.class.getName());
         builder.addConstructorParameter(SessionBeanType.class.getName(), builder.createInject(sessionBeanTypeName));
         builder.addPropertyMetaData("globalContext", builder.createInject("NameSpaces", "globalContext"));
         builder.addPropertyMetaData("proxyFactory", new LegacyProxyFactory());
         builder.setStart("bind");
         builder.setStop("unbind");

         unit.getParent().addAttachment(beanInstanceName, builder.getBeanMetaData());
      }
   }
}
