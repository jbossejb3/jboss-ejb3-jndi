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
import org.jboss.switchboard.javaee.environment.EJBReferenceType;
import org.jboss.switchboard.javaee.jboss.environment.JBossEjbRefType;
import org.jboss.switchboard.javaee.util.InjectionTargetUtil;
import org.jboss.switchboard.mc.spi.MCBasedResourceProvider;
import org.jboss.switchboard.spi.Resource;

/**
 * {@link MCBasedResourceProvider} for processing ejb-ref references in a 
 * Java EE component.
 * 
 * @see #provide(DeploymentUnit, JBossEjbRefType)
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJBRefResourceProvider implements MCBasedResourceProvider<JBossEjbRefType>
{

   /**
    * EJB binder resolver
    */
   private EJBBinderResolver ejbBinderResolver;
   
   /**
    * 
    * @param ejbBinderResolver
    */
   public EJBRefResourceProvider(EJBBinderResolver ejbBinderResolver)
   {
      this.ejbBinderResolver = ejbBinderResolver;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<JBossEjbRefType> getEnvironmentEntryType()
   {
      return JBossEjbRefType.class;
   }

   /**
    *  Generates a {@link Resource resource} from a {@link JBossEjbRefType ejb-ref reference}.
    *  The {@link JBossEjbRefType ejb-ref reference} is resolved to a 
    *  EJB3.1 global JNDI name and a {@link EJBBinder} and a {@link Resource resource} containing this information
    *  is returned.
    *  <p>
    *   Sometimes, for example in the case of EJB2.x Entity beans, the {@link EJBBinder} will not be present
    *   (since {@link EJBBinder EJBBinders} are only for session beans. In such cases, this method return a 
    *   {@link Resource resource} for the just the resolved jndi name.  
    *  </p>
    *  <p>
    *  If the {@link JBossEjbRefType ejb-ref reference} explicitly marks the <code>mapped-name</code>
    *  or <code>lookup</code> or (JBoss specific) <code>jndi-name</code>, then the process of resolving the {@link EJBBinder}
    *  is skipped and a {@link LinkRefResource} is returned 
    *  </p>
    */
   @Override
   public Resource provide(DeploymentUnit unit, JBossEjbRefType ejbRef)
   {
      // first check lookup name
      String lookupName = ejbRef.getLookupName();
      if (lookupName != null && !lookupName.trim().isEmpty())
      {
         return new LinkRefResource(lookupName, true);
      }

      // now check mapped name
      String mappedName = ejbRef.getMappedName();
      if (mappedName != null && !mappedName.trim().isEmpty())
      {
         return new LinkRefResource(mappedName, true);
      }
      
      // now check (JBoss specific) jndi name!
      String jndiName = ejbRef.getJNDIName();
      if (jndiName != null && !jndiName.trim().isEmpty())
      {
         return new LinkRefResource(jndiName, true);
      }
      // get the bean interface type
      String beanInterface = this.getBeanInterfaceType(unit.getClassLoader(), ejbRef);
      // create the EJB reference
      EJBReference reference = new EJBReference(unit, ejbRef.getLink(), beanInterface, mappedName, lookupName);
      // resolve
      EJBBinderResolutionResult result = this.ejbBinderResolver.resolveEJBBinder(unit, reference);
      // thrown an error, if we couldn't resolve the reference
      if (result == null)
      {
         throw new RuntimeException("Could not resolve ejb-ref reference: " + reference + " for environment entry: " + ejbRef.getName() + " in unit " + unit);
      }
      // The EJBBinder might not be available (for example, EJB2.x Entity beans). In such
      // cases, just create a LinkRefResource for the resolved jndi name
      if (result.getEJBBinderName() == null)
      {
         return new LinkRefResource(result.getJNDIName(), true);
      }
      
      // return the resource
      return new EJBRefResource(result.getJNDIName(), result.getEJBBinderName(), result.getBeanMetadata().getContainerName());
   }

   /**
    * Returns the fully qualified class name of the EJB reference. If it's not explicitly
    * set in the passed {@link EJBReferenceType ejb-ref reference}, then the 
    * injection target is used to pick up the bean interface type
    * 
    * @param cl
    * @param ejbRef
    * @return
    */
   private String getBeanInterfaceType(ClassLoader cl, EJBReferenceType ejbRef)
   {
      // first check whether home is explicitly specified
      String explicitHome = ejbRef.getHome();
      if (explicitHome != null && !explicitHome.trim().isEmpty())
      {
         return explicitHome;
      }
      // check whether remote interface is explicitly specified
      String explicitRemote = ejbRef.getRemote();
      if (explicitRemote != null && !explicitRemote.trim().isEmpty())
      {
         return explicitRemote;
      }
      // find from injection target
      Class<?> type = InjectionTargetUtil.getInjectionTargetPropertyType(cl, ejbRef);
      return type == null ? null : type.getName();
   }

}
