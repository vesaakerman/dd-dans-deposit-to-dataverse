/**
 * Copyright (C) 2020 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.dd2d.dataverse

import java.io.PrintStream
import java.net.URI

import better.files.File
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import scalaj.http.HttpResponse

import scala.util.Try

class Dataverse(dvId: String, configuration: DataverseInstanceConfig)(implicit resultOutput: PrintStream) extends HttpSupport with DebugEnhancedLogging {
  trace(dvId)
  protected val connectionTimeout: Int = configuration.connectionTimeout
  protected val readTimeout: Int = configuration.readTimeout
  protected val baseUrl: URI = configuration.baseUrl
  protected val apiToken: String = configuration.apiToken
  protected val apiVersion: String = configuration.apiVersion

  /*
   * Operations
   */
  def create(jsonDef: File): Try[HttpResponse[Array[Byte]]] = {
    trace(jsonDef)
    tryReadFileToString(jsonDef).flatMap(postJson(s"dataverses/$dvId")(201))
  }

  def view(): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    get(s"dataverses/$dvId")
  }

  def delete(): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    deletePath(s"dataverses/$dvId")
  }

  def show(): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    get(s"dataverses/$dvId/contents")
  }

  def listRoles(): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    get(s"dataverses/$dvId/roles")
  }

  def createRole(jsonDef: File): Try[HttpResponse[Array[Byte]]] = {
    trace(jsonDef)
    tryReadFileToString(jsonDef).flatMap(postJson(s"dataverses/$dvId/roles")(200))
  }

  def listFacets(): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    get(s"dataverses/$dvId/facets")
  }

  def setFacets(facets: Seq[String]): Try[HttpResponse[Array[Byte]]] = {
    trace(facets)
    postJson(s"dataverses/$dvId/facets")(200)(facets.map(s => s""""$s"""").mkString("[", ",", "]"))
  }

  def listRoleAssignments(): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    get(s"dataverses/$dvId/assignments")
  }

  // TODO: find out why it doesn't work
  def setDefaultRole(role: String): Try[HttpResponse[Array[Byte]]] = {
    trace(role)
    put(s"dataverses/$dvId/defaultContributorRole/$role")(null)
  }

  def assignRole(jsonDef: File): Try[HttpResponse[Array[Byte]]] = {
    trace(jsonDef)
    tryReadFileToString(jsonDef).flatMap(postJson(s"dataverses/$dvId/assignments")(200))
  }

  def unassignRole(assignmentId: String): Try[HttpResponse[Array[Byte]]] = {
    trace(assignmentId)
    deletePath(s"dataverses/$dvId/assignments/$assignmentId")
  }

  def listMetadataBocks(): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    get(s"dataverses/$dvId/metadatablocks")
  }

  def setMetadataBlocks(mdBlockIds: Seq[String]): Try[HttpResponse[Array[Byte]]] = {
    trace(mdBlockIds)
    postJson(s"dataverses/$dvId/metadatablocks")(200)(mdBlockIds.map(s => s""""$s"""").mkString("[", ",", "]"))
  }

  def isMetadataBlocksRoot: Try[HttpResponse[Array[Byte]]] = {
    trace(())
    get(s"dataverses/$dvId/metadatablocks/isRoot")
  }

  def setMetadataBlocksRoot(isRoot: Boolean): Try[HttpResponse[Array[Byte]]] = {
    trace(isRoot)
    put(s"dataverses/$dvId/metadatablocks/isRoot")(isRoot.toString.toLowerCase)
  }

  def uploadFileToDataset(dvId: String, file: File, jsonMetadata: Option[String]): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    postFile(s"datasets/:persistentId/add?persistentId=$dvId", file, jsonMetadata)(201)
  }

  def createDataset(json: File): Try[HttpResponse[Array[Byte]]] = {
    trace(json)
    tryReadFileToString(json).flatMap(postJson(s"dataverses/$dvId/datasets")(201))
  }

  def createDataset(json: String): Try[HttpResponse[Array[Byte]]] = {
    trace(json)
    postJson(s"dataverses/$dvId/datasets")(201)(json)
  }

  def importDataset(importFile: File, isDdi: Boolean = false, pid: String, keepOnDraft: Boolean = false): Try[HttpResponse[Array[Byte]]] = {
    trace(importFile)
    tryReadFileToString(importFile).flatMap(postJson(s"dataverses/$dvId/datasets/:import${
      if (isDdi) "ddi"
      else ""
    }?pid=$pid&release=${ !keepOnDraft }")(201))
  }

  def publish(): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    postJson(s"dataverses/$dvId/actions/:publish")(200)()
  }
}