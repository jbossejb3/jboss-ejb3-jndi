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
package org.jboss.ejb3.jndi.deployers.resolver;

import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * EJBReference
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJBReference
{

   /**
    * The deployment unit in which this {@link EJBReference} is declared.
    * This will be used while resolving the path for a ejbLink (which can point to a relative
    * path using the # syntax)  
    */
   private DeploymentUnit unit;

   /**
    * The name of the target EJB
    */
   private String beanName;

   /**
    * The fully-qualified name of the target interface (EJB 3.x Business or EJB 2.x Home).
    * In case of no-interface view, this corresponds to the fully qualified class name of
    * the bean class. 
    */
   private String beanInterface;

   /**
    * The mapped-name used for the target
    */
   private String mappedName;
   
   /**
    * The lookup name used for the reference
    */
   private String lookupName;

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    */
   public EJBReference(DeploymentUnit du, String beanName, String interfaceFqn, String mappedName, String lookupName)
   {
      this.unit = du;
      this.beanName = beanName;
      this.beanInterface = interfaceFqn;
      this.mappedName = mappedName;
      this.lookupName = lookupName;
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public String getBeanName()
   {
      return beanName;
   }

   public String getBeanInterface()
   {
      return beanInterface;
   }

   public String getMappedName()
   {
      return mappedName;
   }

   public DeploymentUnit getOwnerDeploymentUnit()
   {
      return this.unit;
   }
   

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   public String toString()
   {
      // Initialize
      StringBuffer buffer = new StringBuffer();

      // Construct
      buffer.append("[EJB Reference: beanInterface '");
      buffer.append(this.beanInterface);
      buffer.append("', beanName '");
      buffer.append(this.beanName);
      buffer.append("', mappedName '");
      buffer.append(this.mappedName);
      buffer.append("', lookupName '");
      buffer.append(this.lookupName);
      buffer.append("', owning unit '");
      buffer.append(this.unit);
      buffer.append("']");

      // Return
      return buffer.toString();
   }

}
