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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationHandler;
import java.util.EventListener;

import javax.naming.NamingException;

import org.jboss.ejb3.jndi.binder.EJBBinder;
import org.jboss.ejb3.jndi.binder.impl.View;
import org.jboss.ejb3.jndi.binder.metadata.SessionBeanType;
import org.jboss.ejb3.jndi.binder.spi.ProxyFactory;
import org.jboss.ejb3.jndi.binder.test.common.AbstractNamingTestCase;
import org.jboss.reloaded.naming.CurrentComponent;
import org.jboss.reloaded.naming.spi.JavaEEApplication;
import org.jboss.reloaded.naming.spi.JavaEEModule;
import org.junit.Test;

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
         StringBuilder sb = new StringBuilder();
         sb.append(view.getMetadata().getName());
         if (view.getBusinessInterface() != null)
         {
            sb.append("#");
            sb.append(view.getBusinessInterface().getName());
         }
         return sb.toString();
      }
   }

   private interface DummyHome
   {
      
   }
   
   private interface DummyLocalHome
   {
      
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
      doReturn(DummyHome.class).when(bean).getHome();
      doReturn(DummyLocalHome.class).when(bean).getLocalHome();
      doReturn(SimpleTestCase.class).when(bean).getEJBClass();
      doReturn(true).when(bean).isLocalBean();
      EJBBinder binder = new EJBBinder(bean);
      binder.setGlobalContext(javaGlobal);
      binder.setProxyFactory(new MyProxyFactory());
      binder.bind();

      CurrentComponent.push(bean);
      try
      {
         // Business local
         String expected = "TestBean#" + InvocationHandler.class.getName();

         assertEquals(expected, iniCtx.lookup("java:global/testApp/testModule/TestBean!" + InvocationHandler.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:app/testModule/TestBean!" + InvocationHandler.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:module/TestBean!" + InvocationHandler.class.getName()));

         // Business remote
         expected = "TestBean#" + EventListener.class.getName();
         
         assertEquals(expected, iniCtx.lookup("java:global/testApp/testModule/TestBean!" + EventListener.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:app/testModule/TestBean!" + EventListener.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:module/TestBean!" + EventListener.class.getName()));
         
         // Home interface
         expected = "TestBean#" + DummyHome.class.getName();
         
         assertEquals(expected, iniCtx.lookup("java:global/testApp/testModule/TestBean!" + DummyHome.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:app/testModule/TestBean!" + DummyHome.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:module/TestBean!" + DummyHome.class.getName()));

         // Local home interface
         expected = "TestBean#" + DummyLocalHome.class.getName();
         
         assertEquals(expected, iniCtx.lookup("java:global/testApp/testModule/TestBean!" + DummyLocalHome.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:app/testModule/TestBean!" + DummyLocalHome.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:module/TestBean!" + DummyLocalHome.class.getName()));

         // no-interface view
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
   
   /**
    * Tests the jndi bindings of a bean which exposes just 1 view
    */
   @Test
   public void testBeanWithSingleView() throws Exception
   {
      // create mock naming constructs
      JavaEEApplication app = mock(JavaEEApplication.class);
      doReturn("testApp").when(app).getName();
      doReturn(createContext()).when(app).getContext();
      doReturn(true).when(app).isEnterpriseApplicationArchive();
      JavaEEModule module = mock(JavaEEModule.class);
      doReturn("testModule").when(module).getName();
      doReturn(createContext()).when(module).getContext();
      doReturn(app).when(module).getApplication();
      
      // create mock bean
      SessionBeanType bean = mock(SessionBeanType.class);
      doReturn(SimpleTestCase.class).when(bean).getEJBClass();
      // override the name of the bean
      doReturn("TestBean").when(bean).getName();
      doReturn(module).when(bean).getModule();
      // just expose the no-interface view
      doReturn(true).when(bean).isLocalBean();
      
      EJBBinder binder = new EJBBinder(bean);
      binder.setGlobalContext(javaGlobal);
      binder.setProxyFactory(new MyProxyFactory());
      binder.bind();

      CurrentComponent.push(bean);
      try
      {
         // no-interface view
         String expected = "TestBean#" + SimpleTestCase.class.getName();

         assertEquals(expected, iniCtx.lookup("java:global/testApp/testModule/TestBean!" + SimpleTestCase.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:app/testModule/TestBean!" + SimpleTestCase.class.getName()));
         assertEquals(expected, iniCtx.lookup("java:module/TestBean!" + SimpleTestCase.class.getName()));
         
         // the additional JNDI bindings as expected by the spec (for a bean exposing just 1 view)
         assertEquals(expected, iniCtx.lookup("java:global/testApp/testModule/TestBean"));
         assertEquals(expected, iniCtx.lookup("java:app/testModule/TestBean"));
         assertEquals(expected, iniCtx.lookup("java:module/TestBean"));
      }
      finally
      {
         CurrentComponent.pop();

         binder.unbind();
      }
   }
}
