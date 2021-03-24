/**
 * This work is protected under copyrights held by the members of the
 * TOOP Project Consortium as indicated at
 * http://wiki.ds.unipi.gr/display/TOOP/Contributors
 * (c) 2018-2021. All rights reserved.
 *
 * This work is licensed under the EUPL 1.2.
 *
 *  = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL
 * (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *         https://joinup.ec.europa.eu/software/page/eupl
 */
package eu.toop.simulator.mock;

import java.util.Objects;

import com.helger.xsds.bdxr.smp1.DocumentIdentifierType;
import com.helger.xsds.bdxr.smp1.ParticipantIdentifierType;

/**
 * This class is a wrapper of SMP ServiceMetadataType queries, used as a key
 *
 * @author yerlibilgin
 */
public class SMPServiceMetadataKey {
  private ParticipantIdentifierType participantIdentifierType;
  private DocumentIdentifierType documentIdentifierType;

  /**
   * Instantiates a new Smp service metadata entry.
   */
  public SMPServiceMetadataKey() {
  }


  public SMPServiceMetadataKey(ParticipantIdentifierType participantIdentifierType, DocumentIdentifierType documentIdentifierType) {
    this.participantIdentifierType = participantIdentifierType;
    this.documentIdentifierType = documentIdentifierType;
  }

  public ParticipantIdentifierType getParticipantIdentifierType() {
    return participantIdentifierType;
  }

  public void setParticipantIdentifierType(ParticipantIdentifierType participantIdentifierType) {
    this.participantIdentifierType = participantIdentifierType;
  }

  public DocumentIdentifierType getDocumentIdentifierType() {
    return documentIdentifierType;
  }

  public void setDocumentIdentifierType(DocumentIdentifierType documentIdentifierType) {
    this.documentIdentifierType = documentIdentifierType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SMPServiceMetadataKey that = (SMPServiceMetadataKey) o;
    return Objects.equals(participantIdentifierType, that.participantIdentifierType) &&
        Objects.equals(documentIdentifierType, that.documentIdentifierType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(participantIdentifierType, documentIdentifierType);
  }

  @Override
  public String toString() {
    return "SMPServiceMetadataKey{" +
        "participantIdentifierType=" + participantIdentifierType +
        ", documentIdentifierType=" + documentIdentifierType +
        '}';
  }
}
