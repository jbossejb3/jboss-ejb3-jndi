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

import javax.naming.LinkRef;

import org.jboss.ejb3.jndi.binder.EJBBinder;
import org.jboss.switchboard.spi.Resource;

/**
 * Represents a {@link Resource resource} for a ejb-local-ref, ejb-ref or a
 * annotated EJB reference.
 * 
 * <p>
 *  The {@link #getTarget() target} of this {@link EJBRefResource resource} is 
 *  a {@link LinkRef} to the jndi name of the target EJB.
 *  
 *   Optionally, this {@link EJBRefResource resource} {@link #getDependency() depends}
 *   on the {@link EJBBinder} MC bean which is responsible for setting up the jndi name
 * </p>
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJBRefResource implements Resource
{

   /**
    * {@link LinkRef} to the jndi name of the target EJB 
    */
   private LinkRef linkRef;
   
   /**
    * (Optional) MC bean name of the {@link EJBBinder EJB binder} which is responsible
    * for binding setting up the jndi binding
    */
   private Object ejbBinderName;
   
   /**
    * The dependency which needs to be resolved before this {@link EJBRefResource} can be 
    * looked up or invoked upon
    */
   private Object invocationDependency;
   
   /**
    * Creates a {@link EJBRefResource}
    * @param ejbJndiName The target jndi name 
    * @param binderName The (optional) MC bean name of the {@link EJBBinder}
    * @param invocationDependency The dependency which needs to be resolved before this {@link EJBRefResource} can be 
    *                           looked up or invoked upon
    * 
    * @throws IllegalArgumentException If <code>ejbJndiName</code> is null or an empty string
    */
   public EJBRefResource(String ejbJndiName, String binderName, Object invocationDependency)
   {
      if (ejbJndiName == null || ejbJndiName.trim().isEmpty())
      {
         throw new IllegalArgumentException("JNDI name cannot be null or empty for " + EJBRefResource.class);
      }
      this.linkRef = new LinkRef(ejbJndiName);
      this.ejbBinderName = binderName;
      this.invocationDependency = invocationDependency;
   }

   /**
    * Returns the dependency (if any)
    */
   @Override
   public Object getDependency()
   {
      return this.ejbBinderName;
   }

   /**
    * Returns the {@link LinkRef} to the target jndi name of the EJB reference
    */
   @Override
   public Object getTarget()
   {
      return this.linkRef;
   }

   @Override
   public Object getInvocationDependency()
   {
      return this.invocationDependency;
   }
   
   @Override
   public String toString()
   { 
      return "EJBRefResource [link-ref: " + this.linkRef + " binder dependency: " + this.ejbBinderName + "]";
   }
}
