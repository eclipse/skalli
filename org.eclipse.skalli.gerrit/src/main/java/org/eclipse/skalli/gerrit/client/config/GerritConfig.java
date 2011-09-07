/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.gerrit.client.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("gerrit")
public class GerritConfig {

  private String host;
  private String port;
  private String user;
  private String privateKey;
  private String passphrase;
  private String contact;

  public String getHost() {
    return host;
  }
  public void setHost(String host) {
    this.host = host;
  }
  public String getPort() {
    return port;
  }
  public void setPort(String port) {
    this.port = port;
  }
  public String getUser() {
    return user;
  }
  public void setUser(String user) {
    this.user = user;
  }
  public String getPrivateKey() {
    return privateKey;
  }
  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }
  public String getPassphrase() {
    return passphrase;
  }
  public void setPassphrase(String passphrase) {
    this.passphrase = passphrase;
  }
  public void setContact(String contact) {
    this.contact = contact;
  }
  public String getContact() {
    return contact;
  }

}
