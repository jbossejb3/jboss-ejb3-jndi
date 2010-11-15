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

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.jndi.binder.EJBBinder;
import org.jboss.ejb3.jndi.deployers.resolver.EJBBinderResolutionResult;
import org.jboss.ejb3.jndi.deployers.resolver.EJBBinderResolver;
import org.jboss.ejb3.jndi.deployers.resolver.EJBReference;
import org.jboss.switchboard.impl.resource.LinkRefResource;
import org.jboss.switchboard.javaee.environment.EJBLocalReferenceType;
import org.jboss.switchboard.javaee.jboss.environment.JBossEjbLocalRefType;
import org.jboss.switchboard.javaee.util.InjectionTargetUtil;
import org.jboss.switchboard.mc.spi.MCBasedResourceProvider;
import org.jboss.switchboard.spi.Resource;

/**
 * {@link MCBasedResourceProvider} for processing ejb-local-ref references in a 
 * Java EE component.
 * 
 * @see #provide(DeploymentUnit, JBossEjbLocalRefType)
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJBLocalRefResourceProvider implements MCBasedResourceProvider<JBossEjbLocalRefType>
{

   /**
    * EJB Binder resolver
    */
   private EJBBinderResolver ejbBinderResolver;
   
   /**
    * 
    * @param ejbBinderResolver
    */
   public EJBLocalRefResourceProvider(EJBBinderResolver ejbBinderResolver)
   {
      this.ejbBinderResolver = ejbBinderResolver;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<JBossEjbLocalRefType> getEnvironmentEntryType()
   {
      return JBossEjbLocalRefType.class;
   }

   /**
    *  Generates a {@link Resource resource} from a {@link JBossEjbLocalRefType ejb-local-ref reference}.
    *  The {@link JBossEjbLocalRefType ejb-local-ref reference} is resolved to a 
    *  EJB3.1 global JNDI name and a {@link EJBBinder} and a {@link Resource resource} containing this information
    *  is returned.
    *  <p>
    *   Sometimes, for example in the case of EJB2.x Entity beans, the {@link EJBBinder} will not be present
    *   (since {@link EJBBinder EJBBinders} are only for session beans. In such cases, this method return a 
    *   {@link Resource resource} for the just the resolved jndi name.  
    *  </p>
    *  <p>
    *  If the {@link JBossEjbLocalRefType ejb-local-ref reference} explicitly marks the <code>mapped-name</code>
    *  or <code>lookup</code> or (JBoss specific) <code>jndi-name</code>, then the process of resolving the {@link EJBBinder}
    *  is skipped and a {@link LinkRefResource} is returned 
    *  </p>
    */
   @Override
   public Resource provide(DeploymentUnit unit, JBossEjbLocalRefType ejbLocalRef)
   {
      // first check lookup name
      String lookupName = ejbLocalRef.getLookupName();
      if (lookupName != null && !lookupName.trim().isEmpty())
      {
         return new LinkRefResource(lookupName, ejbLocalRef.isIgnoreDependency());
      }

      // now check mapped name
      String mappedName = ejbLocalRef.getMappedName();
      if (mappedName != null && !mappedName.trim().isEmpty())
      {
         return new LinkRefResource(mappedName, ejbLocalRef.isIgnoreDependency());
      }
      
      // now check (JBoss specific) jndi name!
      String jndiName = ejbLocalRef.getJNDIName();
      if (jndiName != null && !jndiName.trim().isEmpty())
      {
         return new LinkRefResource(jndiName, ejbLocalRef.isIgnoreDependency());
      }
      // get the bean interface type
      String beanInterface = this.getBeanInterfaceType(unit.getClassLoader(), ejbLocalRef);
      // create a EJB reference
      EJBReference ejbReference = new EJBReference(unit, ejbLocalRef.getLink(), beanInterface, mappedName, lookupName);
      // resolve 
      EJBBinderResolutionResult result = this.ejbBinderResolver.resolveEJBBinder(unit, ejbReference);
      // throw an error, if we couldn't resolve the reference
      if (result == null)
      {
         throw new RuntimeException("Could not resolve ejb-local-ref reference: " + ejbReference + " for environment entry: " + ejbLocalRef.getName() + " in unit " + unit);
      }
      // The EJBBinder might not be available (for example, EJB2.x Entity beans). In such
      // cases, just create a LinkRefResource for the resolved jndi name
      if (result.getEJBBinderName() == null)
      {
         return new LinkRefResource(result.getJNDIName(), ejbLocalRef.isIgnoreDependency());
      }
      // return the resource
      return new EJBRefResource(result.getJNDIName(), result.getEJBBinderName());
   }

   /**
    * Returns the fully qualified class name of the EJB reference. If it's not explicitly
    * set in the passed {@link EJBLocalReferenceType ejb-local-ref reference}, then the 
    * injection target is used to pick up the bean interface type  
    * 
    */
   private String getBeanInterfaceType(ClassLoader cl, EJBLocalReferenceType ejbLocalRef)
   {
      // first check whether local-home is explicitly specified
      String explicitLocalHome = ejbLocalRef.getLocalHome();
      if (explicitLocalHome != null && !explicitLocalHome.trim().isEmpty())
      {
         return explicitLocalHome;
      }
      // check whether local interface is explicitly specified
      String explicitLocal = ejbLocalRef.getLocal();
      if (explicitLocal != null && !explicitLocal.trim().isEmpty())
      {
         return explicitLocal;
      }
      // find from injection target
      Class<?> type = InjectionTargetUtil.getInjectionTargetPropertyType(cl, ejbLocalRef);
      return type == null ? null : type.getName();
   }

}
