JBoss EJB 3 JNDI Binder Requirements
====================================

This component implements the requirements specified in EJB 3.1 4.4 Global
JNDI Access with the exception of 4.4.1.1.1.

It consists of two modules:
 1. binder, the core component which binds proxies into the proper JNDI namespaces
 2. deployers, integration with the Virtual Deployer Framework