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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.jndi.deployers.EJBBinderIdentifierGenerator;
import org.jboss.logging.Logger;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.metadata.ejb.jboss.InvokerBindingMetaData;
import org.jboss.metadata.ejb.jboss.InvokerBindingsMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossEntityBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.BusinessLocalsMetaData;
import org.jboss.metadata.ejb.spec.BusinessRemotesMetaData;
import org.jboss.reloaded.naming.deployers.javaee.JavaEEComponentInformer;

/**
 * ScopedEJBBinderResolver
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class ScopedEJBBinderResolver implements EJBBinderResolver
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(ScopedEJBBinderResolver.class);
   
   private JavaEEComponentInformer componentInformer;
   
   public ScopedEJBBinderResolver(JavaEEComponentInformer componentInformer)
   {
      this.componentInformer = componentInformer;
   }


   /**
    * This method first tries to resolve the passed {@link EjbReference} in the passed <code>du</code>.
    * If the jndi name cannot be resolved in that {@link DeploymentUnit}, then it tries to <i>recursively</i> resolve the reference
    * in the child {@link DeploymentUnit}s of that {@link DeploymentUnit}. If the jndi-name still can't be resolved, then
    * this method recursively repeats the resolution steps with the parent of the passed {@link DeploymentUnit}
    * 
    * <p>
    *   If the jndi-name cannot be resolved in any of the {@link DeploymentUnit}s in the hierarchy, then this method
    *   returns null. Else it returns the resolved jndi-name.
    * </p>
    *  
    * @param du The deployment unit within which the {@link EjbReference} will be resolved
    * @param alreadyScannedDUs The {@link DeploymentUnit}s which have already been scanned for resolving the {@link EjbReference}
    * @param reference The {@link EjbReference} which is being resolved
    * @return Returns the jndi-name resolved out the {@link EjbReference}. If the jndi-name cannot be resolved, then this
    *           method returns null.
    */
   @Override
   public EJBBinderResolutionResult resolveEJBBinder(DeploymentUnit unit, EJBReference ejbRef)
   {
      return this.resolveEJBBinder(unit, new HashSet<DeploymentUnit>(), ejbRef);
   }
   
   /**
    * This method first tries to resolve the passed {@link EjbReference} in the passed <code>du</code>.
    * If the jndi name cannot be resolved in that {@link DeploymentUnit}, then it tries to <i>recursively</i> resolve the reference
    * in the child {@link DeploymentUnit}s of that {@link DeploymentUnit}. If the jndi-name still can't be resolved, then
    * this method recursively repeats the resolution steps with the parent of the passed {@link DeploymentUnit}
    * 
    * <p>
    *   If the jndi-name cannot be resolved in any of the {@link DeploymentUnit}s in the hierarchy, then this method
    *   returns null. Else it returns the resolved jndi-name.
    * </p>
    *  
    * @param du The deployment unit within which the {@link EjbReference} will be resolved
    * @param alreadyScannedDUs The {@link DeploymentUnit}s which have already been scanned for resolving the {@link EjbReference}
    * @param reference The {@link EjbReference} which is being resolved
    * @return Returns the jndi-name resolved out the {@link EjbReference}. If the jndi-name cannot be resolved, then this
    *           method returns null.
    */
   private EJBBinderResolutionResult resolveEJBBinder(DeploymentUnit du, Collection<DeploymentUnit> alreadyScannedDUs, EJBReference reference)
   {
      // first find in the passed DU
      EJBBinderResolutionResult binderResoultionResult = findBinder(du, reference);
      // found, just return it
      if (binderResoultionResult != null)
      {
         return binderResoultionResult;
      }

      if (alreadyScannedDUs == null)
      {
         alreadyScannedDUs = new HashSet<DeploymentUnit>();
      }
      // add this DU to the already scanned DU collection
      alreadyScannedDUs.add(du);

      // jndi-name not resolved in the passed DU, so let's
      // check try resolving in its children DUs
      List<DeploymentUnit> children = du.getChildren();
      if (children != null)
      {
         for (DeploymentUnit child : children)
         {
            // already searched that one
            if (alreadyScannedDUs.contains(child))
            {
               continue;
            }
            // try resolving in this child DU
            binderResoultionResult = this.resolveEJBBinder(child, alreadyScannedDUs, reference);
            // found in this child DU (or its nested child), return the jndi name
            if (binderResoultionResult != null)
            {
               return binderResoultionResult;
            }
            // add the child DU to the already scanned DU collection
            // so that we don't scan it again
            alreadyScannedDUs.add(child);
         }
      }


      // we haven't yet resolved the jndi-name, so let's
      // try resolving in our parent (and any of its children)
      DeploymentUnit parent = du.getParent();
      if (parent != null)
      {
         return this.resolveEJBBinder(parent, alreadyScannedDUs, reference);
      }
      // couldn't resolve in the entire DU hierarchy, return null
      return null;
   }

   private EJBBinderResolutionResult findBinder(DeploymentUnit du, EJBReference reference)
   {
      // TODO: It's a bit too much to add an dependency on jboss-ejb3-common just for this constant attachment name.
      // So this hardcoding. 
      JBossMetaData metadata = du.getAttachment("processed." + JBossMetaData.class.getName(), JBossMetaData.class);
      if (metadata == null)
      {
         // try to get just by the type. The Ejb3MetadataProcessingDeployer skips EJB2.x
         // beans. Hence the attachment by name "processed." + JBossMetaData.class.getName()
         // will not be available for EJB2.x beans. So just try by type:
         metadata = du.getAttachment(JBossMetaData.class);
         if (metadata == null)
         {
            // no metadata found
            return null;
         }
      }
      // Initialize
      logger.debug("Resolving reference for " + reference + " in " + metadata);
      List<JBossEnterpriseBeanMetaData> matches = new ArrayList<JBossEnterpriseBeanMetaData>();

      // Get all Enterprise Beans contained in the metadata
      JBossEnterpriseBeansMetaData beans = metadata.getEnterpriseBeans();

      String resolvedInterface = null;
      // Loop through all EJBs
      for (JBossEnterpriseBeanMetaData bean : beans)
      {
         // See if this is a match
         String intf = this.getMatchingInterface(reference, bean, du);
         if (intf != null)
         {
            // mark it as the resolved interface
            resolvedInterface = intf;
            // Add to the matches found
            matches.add(bean);
            logger.debug("Found match in EJB " + bean.getEjbName() + " for " + reference);
         }
      }
      
      if (matches.isEmpty())
      {
         return null;
      }
      // Ensure we've only got one match
      if (matches.size() > 1)
      {
         // If more than one match was found while EJB name was specified, there's a problem in resolution
         // Report error
         throw new RuntimeException("Specified reference " + reference
               + " was matched by more than one EJB: " + matches
               + ".  Specify beanName explciitly or ensure beanInterface is unique.");
      }

      JBossEnterpriseBeanMetaData resolvedBeanMetaData = matches.get(0);
      // generate JNDI name
      String jndiName = this.getJNDIName(du, resolvedBeanMetaData, resolvedInterface);
      String binderName = null;
      // EJBBinder is only for session beans
      if (resolvedBeanMetaData.isSession())
      {
         binderName = EJBBinderIdentifierGenerator.getEJBBinderName(this.componentInformer, du, resolvedBeanMetaData.getEjbName());
      }
      
      EJBBinderResolutionResult result = new EJBBinderResolutionResult(binderName, jndiName, resolvedBeanMetaData);
      logger.debug("Resolved reference: " + reference + " to: " + result);
      return result;

   }

   /**
    * Determines whether the specified session bean is a match for the specified
    * reference
    * 
    * @param reference
    * @param beanMetaData
    * @param cl The ClassLoader for the specified metadata
    * @return
    */
   private String getMatchingInterface(EJBReference reference, JBossEnterpriseBeanMetaData beanMetaData, DeploymentUnit unit)
   {
      // We only work with Session beans and (EJB2.x) entity beans.
      // If it's neither a session bean nor an entity bean, then just return
      if (!beanMetaData.isSession() && !beanMetaData.isEntity())
      {
         return null;
      }
      
      if (!this.acceptsEjbName(beanMetaData, reference, unit))
      {
         return null;
      }
      
      // let's see if this is a no-interface view reference
      if (this.hasNoInterfaceView(beanMetaData))
      {
         if(beanMetaData.getEjbClass().equals(reference.getBeanInterface()))
         {
            // it's a match (due to matching business interface)
            return beanMetaData.getEjbClass();
         }
      }
       
      ClassLoader cl = unit.getClassLoader();
      // Now get the interfaces that are directly eligible on the bean (i.e. business local, business remote,
      // remote home, local home, local, remote interfaces).
      Set<Class<?>> directlyEligibleInterfacesOnBean = this.getExposedInterfaces(beanMetaData, cl);

      // Get the requested bean interface 
      String requestedInterface = reference.getBeanInterface();
      if (requestedInterface == null || requestedInterface.trim().isEmpty())
      {
         throw new RuntimeException("beanInterface missing from ejb reference: " + reference);
      }
      Class<?> type = this.loadClass(requestedInterface, cl);

      // If the directly eligible interfaces on the bean match the requested
      // interface, then we have a match.
      if (directlyEligibleInterfacesOnBean.contains(type))
      {
         return type.getName();
      }
      Class<?> resolvedInterface = null;
      boolean matchFound = false;
      for (Class<?> exposedIntf : directlyEligibleInterfacesOnBean)
      {
         if (type.isAssignableFrom(exposedIntf))
         {
            if (!matchFound)
            {
               resolvedInterface = exposedIntf;
               matchFound = true;
               continue;
            }
            throw new RuntimeException("beanInterface specified, " + type + ", is not unique within EJB " + beanMetaData.getEjbName());
         }
      }
      return resolvedInterface == null ? null : resolvedInterface.getName();
      
   }

   private Set<Class<?>> getExposedInterfaces(JBossEnterpriseBeanMetaData enterpriseBean, ClassLoader cl)
   {
      if (enterpriseBean.isSession() && (enterpriseBean instanceof JBossSessionBeanMetaData))
      {
         return this.getSessionBeanExposedInterfaces((JBossSessionBeanMetaData) enterpriseBean, cl);
      }
      if (enterpriseBean.isEntity() && (enterpriseBean instanceof JBossEntityBeanMetaData))
      {
         return this.getEntityBeanExposedInterfaces((JBossEntityBeanMetaData) enterpriseBean, cl); 
      }
      // return an empty set
      return new HashSet<Class<?>>();
   }

   private Set<Class<?>> getSessionBeanExposedInterfaces(JBossSessionBeanMetaData smd, ClassLoader cl)
   {
      Set<Class<?>> interfaces = new HashSet<Class<?>>();

      // Add all eligible bean interfaces
      BusinessLocalsMetaData businessLocals = smd.getBusinessLocals();
      BusinessRemotesMetaData businessRemotes = smd.getBusinessRemotes();
      String home = smd.getHome();
      String localHome = smd.getLocalHome();
      if (businessLocals != null)
      {
         for (String busLocal : businessLocals)
         {
            if (busLocal == null)
            {
               continue;
            }
            interfaces.add(this.loadClass(busLocal, cl));   
         }
         
      }
      if (businessRemotes != null)
      {
         for (String busRemote : businessRemotes)
         {
            if (busRemote == null)
            {
               continue;
            }
            interfaces.add(this.loadClass(busRemote, cl));   
         }

      }
      if (home != null && home.trim().length() > 0)
      {
         interfaces.add(this.loadClass(home, cl));
      }
      if (localHome != null && localHome.trim().length() > 0)
      {
         interfaces.add(this.loadClass(localHome, cl));
      }

      return interfaces;
   }

   private Set<Class<?>> getEntityBeanExposedInterfaces(JBossEntityBeanMetaData entityBean, ClassLoader cl)
   {
      Set<Class<?>> interfaces = new HashSet<Class<?>>();

      // Add all eligible bean interfaces
      // local
      String local = entityBean.getLocal();
      if (local != null && !local.trim().isEmpty())
      {
         interfaces.add(this.loadClass(local, cl));
      }
      // remote
      String remote = entityBean.getRemote();
      if (remote != null && remote.trim().isEmpty())
      {
         interfaces.add(this.loadClass(remote, cl));
      }
      // remote home
      String home = entityBean.getHome();
      if (home != null && !home.trim().isEmpty())
      {
         interfaces.add(this.loadClass(home, cl));
      }
      // local home
      String localHome = entityBean.getLocalHome();
      if (localHome != null && !localHome.trim().isEmpty())
      {
         interfaces.add(this.loadClass(localHome, cl));
      }

      return interfaces;
   }


   private String getModuleName(DeploymentUnit unit)
   {
      return this.componentInformer.getModuleName(unit);
   }
   
   private String getJNDIName(DeploymentUnit unit, JBossEnterpriseBeanMetaData beanMetaData, String interfaceFQN)
   {
      if (beanMetaData.isSession() && (beanMetaData instanceof JBossSessionBeanMetaData))
      {
         return this.getGlobalJNDINameForSessionBean(unit, (JBossSessionBeanMetaData) beanMetaData, interfaceFQN);
      }
      if (beanMetaData.isEntity() && (beanMetaData instanceof JBossEntityBeanMetaData))
      {
         return this.getJNDINameForEntityBean(unit, (JBossEntityBeanMetaData) beanMetaData, interfaceFQN);
      }
      return null;
   }

   private String getGlobalJNDINameForSessionBean(DeploymentUnit unit, JBossSessionBeanMetaData sessionBean, String interfaceFQN)
   {
      StringBuilder globalJNDIName = new StringBuilder("java:global/");
      DeploymentUnit topLevelUnit = unit.isTopLevel() ? unit : unit.getTopLevel();
      if (topLevelUnit.isAttachmentPresent(JBossAppMetaData.class))
      {
         String earName = topLevelUnit.getSimpleName();
         // strip .ear suffix
         earName = earName.substring(0, earName.length() - 4);
         globalJNDIName.append(earName);
         globalJNDIName.append("/");
      }
      String moduleName = this.getModuleName(unit);
      globalJNDIName.append(moduleName);
      globalJNDIName.append("/");
      globalJNDIName.append(sessionBean.getEjbName());
      if (interfaceFQN != null && !interfaceFQN.trim().isEmpty())
      {
         globalJNDIName.append("!");
         globalJNDIName.append(interfaceFQN);
      }
      
      return globalJNDIName.toString();

   }

   private String getJNDINameForEntityBean(DeploymentUnit unit, JBossEntityBeanMetaData entityBean, String interfaceFQN)
   {
      InvokerBindingsMetaData invokerBindings = entityBean.determineInvokerBindings();
      if (invokerBindings == null || invokerBindings.isEmpty())
      {
         return entityBean.getJndiName();
      }
      
      InvokerBindingMetaData invokerBinding = invokerBindings.iterator().next();
      String jndiName = invokerBinding.getJndiName();
      if(jndiName == null || jndiName.isEmpty())
      {
         jndiName = entityBean.getJndiName();
      }
      return jndiName;
   }
   /**
    * Returns true if the passed session bean metadata represents a EJB3.1 bean
    * which exposes a no-interface view. Else returns false.
    * 
    * @param beanMetaData Session bean metadata
    * @return
    */
   private boolean hasNoInterfaceView(JBossEnterpriseBeanMetaData beanMetaData)
   {
      if (!beanMetaData.isSession() || !(beanMetaData instanceof JBossSessionBeanMetaData))
      {
         return false;
      }
      JBossSessionBeanMetaData sessionBean = (JBossSessionBeanMetaData) beanMetaData;
      if (isEJB31(sessionBean) == false)
      {
         return false;
      }
      if (sessionBean instanceof JBossSessionBean31MetaData == false)
      {
         return false;
      }
      JBossSessionBean31MetaData sessionBean31 = (JBossSessionBean31MetaData) beanMetaData;
      return sessionBean31.isNoInterfaceBean();
   }

   /**
    * Returns true if the passed session bean metadata represents a EJB3.1 bean
    * @param smd Session bean metadata
    * @return
    */
   private boolean isEJB31(JBossSessionBeanMetaData smd)
   {
      JBossMetaData jbossMetaData = smd.getJBossMetaData();
      return jbossMetaData.isEJB31();
   }

   private boolean acceptsEjbName(JBossEnterpriseBeanMetaData beanMetaData, EJBReference reference, DeploymentUnit du)
   {
      // the requested bean interface matches the nointerface view bean class name
      // Now let's see if there's an explicit bean name specified. If such an 
      // explicit bean name is specified then make sure it matches the current bean's name
      String ejbLink = reference.getBeanName();
      if (ejbLink == null || ejbLink.trim().isEmpty())
      {
         return true;
      }
      
      if (ejbLink.endsWith("#") || ejbLink.endsWith("/"))
      {
         throw new RuntimeException("ejbLink: " + ejbLink + " in ejb reference: " + reference + " should not end with a / or a #");
      }
      
      int indexOfHash = ejbLink.indexOf("#");
      int indexOfForwardSlash = ejbLink.indexOf("/");
      if (indexOfHash == -1 && indexOfForwardSlash == -1)
      {
         return ejbLink.equals(beanMetaData.getEjbName());
      }
      
      if (indexOfHash != -1)
      {
         String path = ejbLink.substring(0, indexOfHash);
         String referencedEjbName = ejbLink.substring(indexOfHash + 1, ejbLink.length());
         
         if (path.equals(du.getSimpleName()) && referencedEjbName.equals(beanMetaData.getEjbName()))
         {
            return true;
         }
         DeploymentUnit ownerDeploymentUnit = reference.getOwnerDeploymentUnit();
         DeploymentUnit parentOfOwnerDU = ownerDeploymentUnit.getParent();
         if (parentOfOwnerDU == null)
         {
            throw new RuntimeException("Cannot resolve ejbLink: " + ejbLink + " in reference " + reference
                  + " from unit: " + ownerDeploymentUnit
                  + " because the unit is a top-level unit and hence cannot have relative path reference");
         }
         DeploymentUnit relativeDU = this.getRelativeDeploymentUnit(parentOfOwnerDU, path);
         if (du.getName().equals(relativeDU.getName()) && referencedEjbName.equals(beanMetaData.getEjbName()))
         {
            return true;
         }
         
         return false;
      }
      
      if (indexOfForwardSlash != -1)
      {
         String moduleName = ejbLink.substring(0, indexOfForwardSlash);
         String referencedEjbName = ejbLink.substring(indexOfForwardSlash + 1, ejbLink.length());
         
         if (moduleName.equals(this.getModuleName(du)) && referencedEjbName.equals(beanMetaData.getEjbName()))
         {
            return true;
         }
         return false;
      }
      
      return ejbLink.equals(beanMetaData.getEjbName());
   }

   private DeploymentUnit getRelativeDeploymentUnit(DeploymentUnit current, String path)
   {
      String relativePathFromTopLevelDU = current.getRelativePath();
      if (relativePathFromTopLevelDU.isEmpty())
      {
         relativePathFromTopLevelDU = path;
      }
      else
      {
         relativePathFromTopLevelDU = relativePathFromTopLevelDU + File.pathSeparator + path;
      }
      for (DeploymentUnit child : current.getChildren())
      {
         if (child.getRelativePath().equals(relativePathFromTopLevelDU))
         {
            return child;
         }
      }
      throw new IllegalArgumentException("Can't find a deployment unit with path " + path + " from unit " + current);
   }

   private Class<?> loadClass(String className, ClassLoader cl)
   {
      try
      {
         return cl.loadClass(className);
      }
      catch (ClassNotFoundException cnfe)
      {
         throw new RuntimeException(cnfe);
      }
   }
   
   
}
