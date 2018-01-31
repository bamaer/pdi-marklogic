# pdi-marklogic
MarkLogic steps for Pentaho Data Integration. Uses MarkLogic's Data Movement SDK in MarkLogic version 9.

## Included steps

- MarkLogic Document Output Step - Stream data in to MarkLogic Server documents
- MarkLogic Document Input Step - Stream data out of MarkLogic Server documents

## Installation

Easiest way is to download the binary release.

- Fetch the latest release from here: https://github.com/Pentaho-SE-EMEA-APAC/pdi-marklogic/releases 
- Unzip the zip file
- Take the 'pdi-marklogic' folder and drop in to PENTAHO_HOME/design-tools/data-integration/plugins
- Download the MarkLogic Java SDK zip file from here: http://developer.marklogic.com/products/java 
- Extract the contents of the zip file
- Copy the lib/*.jar files - EXCEPT the slf4j jar - to the plugins/pdi-marklogic/lib folder (create this subfolder if it does not exist)
- Restart PDI/Spoon
- You will find the MarkLogic steps under the 'Big Data' folder

## Samples

Sample transforms are available in the ZIP release, within the samples folder.

## Step Configuration

Create a new Database Connection in PDI - you MUST use the Wizard for this. Select MarkLogic Server as the database

- Host - The MarkLogic Server hostname
- Port - MarkLogic REST API port (use 8000 by default)
- Database - Type 'Documents', or your preferred MarkLogic database name (must already exist)
- Authentication scheme - 'digest' by default in MarkLogic Server
- Username - access username
- Password - access password

When configuring one of the individual steps, you will type in the below fields:-

- Connection - Browse to the connection, or create one by clicking 'Wizard'
- Collection name field - Which PDI field holds the collection name
- Format field - High level MarkLogic Format - XML, JSON, TEXT, BINARY (Default: JSON)
- Content Type field - MIME type of the document (Default: application/json)
- Document URI field (optional)
- Document content field - The field containing JSON/XML for the document
- Document properties field (optional) - The field containing JSON/XML for the document's properties fragment

You will now be able to execute your transformation.

## License and Copyright

All material in this repository are Copyright 2002-2018 Hitachi Vantara. All code is licensed as Apache 2.0 unless explicitly stated. See the LICENSE file for more details.

## Support Statement

This work is at Stage 1 : Development Phase: Start-up phase of an internal project. Usually a Labs experiment. (Unsupported)