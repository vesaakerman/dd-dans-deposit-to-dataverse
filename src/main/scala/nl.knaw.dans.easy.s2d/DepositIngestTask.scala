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
package nl.knaw.dans.easy.s2d

import nl.knaw.dans.easy.s2d.dataverse.DataverseInstance
import nl.knaw.dans.easy.s2d.dataverse.json.{ DatasetVersion, DataverseDataset, MetadataBlock, PrimitiveField }
import nl.knaw.dans.easy.s2d.queue.Task
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.Formats
import org.json4s.native.Serialization
import scala.xml._

import scala.util.Try

/**
 * Checks one deposit and then ingests it into Dataverse.
 *
 * @param deposit the deposit to ingest
 * @param dataverse the Dataverse instance to ingest in
 * @param jsonFormats implicit necessary for pretty-printing JSON
 */
case class DepositIngestTask(deposit: Deposit, dataverse: DataverseInstance)(implicit jsonFormats: Formats) extends Task with DebugEnhancedLogging {
  trace(deposit, dataverse)

  override def run(): Try[Unit] = {
    trace(())
    debug(s"Ingesting $deposit into Dataverse")

    // TODO: validate: is this a deposit can does it contain a bag that conforms to DANS BagIt Profile? (call easy-validate-dans-bag)

    // TODO: make this more robust
    val maybeTitle = deposit.tryDdm map {
      ddm =>
        (ddm \ "profile" \ "title").toList.head.text
    }

    val title = maybeTitle.get

    // Create dataset
    val ds = DataverseDataset(
      DatasetVersion(
        Map(
          "citation" -> MetadataBlock(
            fields = List(
              PrimitiveField(
                typeName = "title",
                value = title,
                multiple = false)
            ),
            displayName = "Citation Metadata"
          )
        )
      )
    )
    dataverse.dataverse("root").createDataset(Serialization.writePretty(ds)).map(_ => ())
  }
}
