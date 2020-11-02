/**
 * Copyright (C) 2018-2020 toop.eu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.toop.simulator.mock;

import com.helger.xsds.bdxr.smp1.DocumentIdentifierType;
import com.helger.xsds.bdxr.smp1.ParticipantIdentifierType;

import java.util.Objects;

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
