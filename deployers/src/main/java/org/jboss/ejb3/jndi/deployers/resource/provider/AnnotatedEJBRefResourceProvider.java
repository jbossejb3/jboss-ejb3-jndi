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

import javax.ejb.EJB;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.jndi.binder.EJBBinder;
import org.jboss.ejb3.jndi.deployers.resolver.EJBBinderResolutionResult;
import org.jboss.ejb3.jndi.deployers.resolver.EJBBinderResolver;
import org.jboss.ejb3.jndi.deployers.resolver.EJBReference;
import org.jboss.switchboard.impl.resource.LinkRefResource;
import org.jboss.switchboard.javaee.environment.AnnotatedEJBRefType;
import org.jboss.switchboard.javaee.jboss.environment.JBossAnnotatedEJBRefType;
import org.jboss.switchboard.javaee.util.InjectionTargetUtil;
import org.jboss.switchboard.mc.spi.MCBasedResourceProvider;
import org.jboss.switchboard.spi.Resource;

/**
 * {@link MCBasedResourceProvider} for processing {@link EJB} annotation references in a 
 * Java EE component.
 * 
 * @see #provide(DeploymentUnit, JBossAnnotatedEJBRefType)
 * 
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class AnnotatedEJBRefResourceProvider extends AbstractEJBResourceProvider implements MCBasedResourceProvider<JBossAnnotatedEJBRefType>
{

   /**
    * 
    * @param ejbBinderResolver
    */
   public AnnotatedEJBRefResourceProvider(EJBBinderResolver ejbBinderResolver)
   {
      super(ejbBinderResolver);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Class<JBossAnnotatedEJBRefType> getEnvironmentEntryType()
   {
      return JBossAnnotatedEJBRefType.class;
   }

   /**
    *  Generates a {@link Resource resource} from a {@link JBossAnnotatedEJBRefType annotated EJB reference}.
    *  The {@link JBossAnnotatedEJBRefType annotated EJB reference} is resolved to a 
    *  EJB3.1 global JNDI name and a {@link EJBBinder} and a {@link Resource resource} containing this information
    *  is returned.
    *  If the {@link JBossAnnotatedEJBRefType annotated EJB reference} explicitly marks the <code>mapped-name</code>
    *  or <code>lookup</code> or (JBoss specific) <code>jndi-name</code>, then the process of resolving the {@link EJBBinder}
    *  is skipped and a {@link LinkRefResource} is returned 
    *  
    */
   @Override
   public Resource provide(DeploymentUnit unit, JBossAnnotatedEJBRefType annotatedEjbRef)
   {
      Resource resource = this.provideJndiNameBasedResource(unit, annotatedEjbRef);
      if (resource != null)
      {
         return resource;
      }
      // get the bean interface type
      String beanInterface = this.getBeanInterfaceType(unit.getClassLoader(), annotatedEjbRef);
      // create the EJB reference to resolve
      EJBReference ejbReference = new EJBReference(unit, annotatedEjbRef.getBeanName(), beanInterface, annotatedEjbRef.getMappedName(), annotatedEjbRef.getLookupName());
      // resolve using the EJBBinder resolver
      EJBBinderResolutionResult result = this.ejbBinderResolver.resolveEJBBinder(unit, ejbReference);
      
      // throw error, if we can't resolve
      if (result == null)
      {
         throw new RuntimeException("Could not resolve @EJB reference: " + ejbReference + " for environment entry: "
               + annotatedEjbRef.getName() + " in unit " + unit);
      }
      
      // get the invocation dependencies 
      Collection<?> invocationDependencies = this.getInvocationDependencies(result);
      // return the resource
      return new EJBRefResource(result.getJNDIName(), result.getEJBBinderName(), invocationDependencies);
   }

   /**
    * Returns the fully qualified class name of the EJB reference. If it's not explicitly
    * set in the passed {@link AnnotatedEJBRefType annotated EJB reference}, then the 
    * injection target is used to pick up the bean interface type  
    * 
    */
   private String getBeanInterfaceType(ClassLoader cl, AnnotatedEJBRefType annotatedEjbRef)
   {
      // first check whether the type is explicitly specified
      String explicitType = annotatedEjbRef.getBeanInterface();
      if (explicitType != null && !explicitType.trim().isEmpty())
      {
         return explicitType;
      }
      Class<?> type = InjectionTargetUtil.getInjectionTargetPropertyType(cl, annotatedEjbRef);
      return type == null ? null : type.getName();
   }

}
