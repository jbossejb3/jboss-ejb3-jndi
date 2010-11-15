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
package org.jboss.ejb3.jndi.deployers;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.jndi.binder.EJBBinder;
import org.jboss.reloaded.naming.deployers.javaee.JavaEEComponentInformer;

/**
 * Generates a name for the {@link EJBBinder}
 * <p>
 *  Each EJB within a {@link DeploymentUnit deployment unit} will have a {@link EJBBinder}
 *  with a unique name. The {@link EJBBinderIdentifierGenerator} is responsible for generating
 *  this name.
 * </p>
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJBBinderIdentifierGenerator
{

   /**
    * Returns the {@link EJBBinder} name under which the {@link EJBBinder} will be installed 
    * in MC, for the passed {@link DeploymentUnit deployment unit} and the EJB.
    * 
    * @param informer {@link JavaEEComponentInformer} which will be used to get the application and module names
    * @param unit The deployment unit
    * @param ejbName The name of the EJB
    * @return
    */
   public static String getEJBBinderName(JavaEEComponentInformer informer, DeploymentUnit unit, String ejbName)
   {
      StringBuilder ejbBinderName = new StringBuilder("jboss.ejb3:");
      String appName = informer.getApplicationName(unit);
      if (appName != null)
      {
         ejbBinderName.append("application=");
         ejbBinderName.append(appName);
         ejbBinderName.append(",");
      }
      String moduleName = informer.getModuleName(unit);
      ejbBinderName.append("module=");
      ejbBinderName.append(moduleName);

      ejbBinderName.append(",component=");
      ejbBinderName.append(ejbName);

      ejbBinderName.append(",service=");
      ejbBinderName.append(EJBBinder.class.getSimpleName());

      return ejbBinderName.toString();

   }
}
