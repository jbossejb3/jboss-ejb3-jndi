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
package org.jboss.ejb3.jndi.binder.test.simple;

import org.jboss.ejb3.jndi.binder.EJBBinder;
import org.jboss.ejb3.jndi.binder.impl.View;
import org.jboss.ejb3.jndi.binder.metadata.SessionBeanType;
import org.jboss.ejb3.jndi.binder.spi.ProxyFactory;
import org.jboss.ejb3.jndi.binder.test.common.AbstractNamingTestCase;
import org.jboss.reloaded.naming.CurrentComponent;
import org.jboss.reloaded.naming.spi.JavaEEApplication;
import org.jboss.reloaded.naming.spi.JavaEEModule;
import org.junit.Test;

import javax.naming.NamingException;
import java.lang.reflect.InvocationHandler;
import java.util.EventListener;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class SimpleTestCase extends AbstractNamingTestCase
{
   private static class MyProxyFactory implements ProxyFactory
   {
      @Override
      public Object produce(View view)
      {
         return view.getMetadata().getName() + "#" + view.getBusinessInterface().getName();
      }
   }

   @Test
   public void test1() throws NamingException
   {
      JavaEEApplication app = mock(JavaEEApplication.class);
      doReturn("testApp").when(app).getName();
      doReturn(createContext()).when(app).getContext();
      doReturn(true).when(app).isEnterpriseApplicationArchive();
      JavaEEModule module = mock(JavaEEModule.class);
      doReturn("testModule").when(module).getName();
      doReturn(createContext()).when(module).getContext();
      doReturn(app).when(module).getApplication();
      SessionBeanType bean = mock(SessionBeanType.class);
      doReturn("TestBean").when(bean).getName();
      doReturn(module).when(bean).getModule();
      doReturn(asList(InvocationHandler.class)).when(bean).getBusinessLocals();
      doReturn(asList(EventListener.class)).when(bean).getBusinessRemotes();
      doReturn(SimpleTestCase.class).when(bean).getEJBClass();
      doReturn(true).when(bean).isLocalBean();
      EJBBinder binder = new EJBBinder(bean);
      binder.setGlobalContext(javaGlobal);
      binder.setProxyFactory(new MyProxyFactory());
      binder.bind();

      CurrentComponent.push(bean);
      try
      {
         String expected = "TestBean#" + InvocationHandler.class.getName();

         assertEquals(expected, iniCtx.lookup("java:global/testApp/testModule/TestBean!" + InvocationHandler.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:app/testModule/TestBean!" + InvocationHandler.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:module/TestBean!" + InvocationHandler.class.getName()));

         expected = "TestBean#" + EventListener.class.getName();
         
         assertEquals(expected, iniCtx.lookup("java:global/testApp/testModule/TestBean!" + EventListener.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:app/testModule/TestBean!" + EventListener.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:module/TestBean!" + EventListener.class.getName()));

         expected = "TestBean#" + SimpleTestCase.class.getName();

         assertEquals(expected, iniCtx.lookup("java:global/testApp/testModule/TestBean!" + SimpleTestCase.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:app/testModule/TestBean!" + SimpleTestCase.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:module/TestBean!" + SimpleTestCase.class.getName()));
      }
      finally
      {
         CurrentComponent.pop();

         binder.unbind();
      }
   }
}
