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
package org.jboss.ejb3.jndi.deployers.proxy;

import org.jboss.ejb3.jndi.binder.impl.View;
import org.jboss.ejb3.jndi.binder.spi.ProxyFactory;
import org.jboss.ejb3.jndi.deployers.metadata.SessionBeanTypeWrapper;
import org.jboss.metadata.ejb.jboss.InvokerBindingMetaData;
import org.jboss.metadata.ejb.jboss.InvokerBindingsMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.jndi.resolver.impl.JNDIPolicyBasedSessionBean31JNDINameResolver;

import javax.naming.LinkRef;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class LegacyProxyFactory implements ProxyFactory
{
   private JNDIPolicyBasedSessionBean31JNDINameResolver nameResolver = new JNDIPolicyBasedSessionBean31JNDINameResolver();

   @Override
   public Object produce(View view)
   {
      SessionBeanTypeWrapper beanType = (SessionBeanTypeWrapper) view.getMetadata();
      JBossSessionBeanMetaData sessionBean = beanType.getSessionBeanMetaData();
      if (!sessionBean.getJBossMetaData().isEJB3x())
      {
         String jndiName = this.getJNDINameForEjb2xSessionBean(sessionBean, view);
         return new LinkRef(jndiName);
      }
      String jndiName = nameResolver.resolveJNDIName(sessionBean, view.getBusinessInterface().getName());
      return new LinkRef(jndiName);
   }
   
   private String getJNDINameForEjb2xSessionBean(JBossSessionBeanMetaData sessionBean, View view)
   {
      View.Type viewType = view.getType();
      if (viewType == View.Type.LOCAL_HOME)
      {
         return sessionBean.determineLocalJndiName();
      }
      
      InvokerBindingsMetaData invokerBindings = sessionBean.determineInvokerBindings();
      if (invokerBindings == null || invokerBindings.isEmpty())
      {
         return sessionBean.determineJndiName();
      }
      
      InvokerBindingMetaData invokerBinding = invokerBindings.iterator().next();
      String jndiName = invokerBinding.getJndiName();
      if(jndiName == null || jndiName.isEmpty())
      {
         jndiName = sessionBean.determineJndiName();
      }
      return jndiName;
   }
}
