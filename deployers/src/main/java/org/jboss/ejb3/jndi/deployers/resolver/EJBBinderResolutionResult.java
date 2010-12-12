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

import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;

/**
 * EJBResolution
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJBBinderResolutionResult
{

   private String jndiName;
   
   private String binderName;
   
   private JBossEnterpriseBeanMetaData beanMetadata;
   
   private String resolvedBusinessInterface;
   
   public EJBBinderResolutionResult(String binderName, String ejbJndiName, JBossEnterpriseBeanMetaData beanMetadata, String resolvedBusinessInterface)
   {
      this.binderName = binderName;
      this.jndiName = ejbJndiName;
      this.beanMetadata = beanMetadata;
      this.resolvedBusinessInterface = resolvedBusinessInterface;
   }
   
   public String getJNDIName()
   {
      return this.jndiName;
   }
   
   public String getEJBBinderName()
   {
      return this.binderName;
   }
   
   public JBossEnterpriseBeanMetaData getBeanMetadata()
   {
      return this.beanMetadata;
   }
   
   public String getResolvedBusinessInterface()
   {
      return this.resolvedBusinessInterface;
   }
   
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
      sb.append("[jndiName=");
      sb.append(this.jndiName);
      sb.append(" ,binderName=");
      sb.append(this.binderName);
      sb.append(" ,resolvedBusinessInterface=");
      sb.append(this.resolvedBusinessInterface);
      sb.append("]");
      return sb.toString();
   }
}
