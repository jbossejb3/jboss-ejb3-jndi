/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.jndi.deployers.resource.provider;

import java.util.Collection;
import java.util.HashSet;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.jndi.deployers.resolver.EJBBinderResolutionResult;
import org.jboss.ejb3.jndi.deployers.resolver.EJBBinderResolver;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;
import org.jboss.switchboard.impl.resource.LinkRefResource;
import org.jboss.switchboard.javaee.jboss.environment.JBossJavaEEResourceType;
import org.jboss.switchboard.spi.Resource;

/**
 * AbstractEJBResourceProvider
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public abstract class AbstractEJBResourceProvider
{

   /**
    * EJB Binder resolver
    */
   protected EJBBinderResolver ejbBinderResolver;

   /**
    * 
    * @param ejbBinderResolver
    */
   public AbstractEJBResourceProvider(EJBBinderResolver ejbBinderResolver)
   {
      this.ejbBinderResolver = ejbBinderResolver;
   }

   protected Resource provideJndiNameBasedResource(DeploymentUnit unit, JBossJavaEEResourceType jbossJavaEEResourceRef)
   {
      // first check lookup name
      String lookupName = jbossJavaEEResourceRef.getLookupName();
      if (lookupName != null && !lookupName.trim().isEmpty())
      {
         return new LinkRefResource(lookupName, null, true);
      }

      // now check mapped name
      String mappedName = jbossJavaEEResourceRef.getMappedName();
      if (mappedName != null && !mappedName.trim().isEmpty())
      {
         return new LinkRefResource(mappedName, null, true);
      }

      // now check (JBoss specific) jndi name!
      String jndiName = jbossJavaEEResourceRef.getJNDIName();
      if (jndiName != null && !jndiName.trim().isEmpty())
      {
         return new LinkRefResource(jndiName, null, true);
      }

      return null;
   }

   protected Collection<?> getInvocationDependencies(EJBBinderResolutionResult binderResolutionResult)
   {
      Collection<String> invocationDependencies = new HashSet<String>();
      JBossEnterpriseBeanMetaData enterpriseBean = binderResolutionResult.getBeanMetadata();
      // add the dependency on the EJB container
      invocationDependencies.add(enterpriseBean.getContainerName());

      if (this.isEJB31SessionBean(enterpriseBean))
      {
         JBossSessionBean31MetaData sessionBean = (JBossSessionBean31MetaData) enterpriseBean;
         // if the resolved bean is a singleton, then add a dependency on the singleton bean jndi binder
         if (sessionBean.isSingleton())
         {
            String singletonBeanJndiBinder = enterpriseBean.getContainerName() + ",type=singleton-bean-jndi-binder";
            invocationDependencies.add(singletonBeanJndiBinder);
         }
         // if the resolved interface is a no-interface view
         // then add a dependency on the no-interface view jndi binder
         String resolvedBusinessInterface = binderResolutionResult.getResolvedBusinessInterface();
         if (sessionBean.isNoInterfaceBean() && sessionBean.getEjbClass().equals(resolvedBusinessInterface))
         {
            String nointerfaceViewJndiBinder = enterpriseBean.getContainerName() + ",type=nointerface-view-jndi-binder";
            invocationDependencies.add(nointerfaceViewJndiBinder);
         }
      }

      return invocationDependencies;
   }

   private boolean isEJB31SessionBean(JBossEnterpriseBeanMetaData enterpriseBean)
   {
      if (!enterpriseBean.getJBossMetaData().isEJB31())
      {
         return false;
      }
      if (!enterpriseBean.isSession())
      {
         return false;
      }
      // (ugly) check
      if (!(enterpriseBean instanceof JBossSessionBean31MetaData))
      {
         return false;
      }
      return true;
   }
}
