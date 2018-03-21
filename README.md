# pdi-marklogic
MarkLogic steps for Pentaho Data Integration. Uses MarkLogic's Data Movement SDK in MarkLogic version 9.

## Included steps

- MarkLogic Document Output Step - Stream data in to MarkLogic Server documents
- MarkLogic Document Input Step - Stream data out of MarkLogic Server documents

## Installation

Esaiest way is via the Pentaho Marketplace:-
- Open Pentaho Data Integration (aka Spoon)
- Open the Tools menu and select Marketplace
- Type in 'MarkLogic' in the search bar
- Install the plugin using the Install link
- Restart Pentaho Data Integration
- You will find the MarkLogic steps under the 'Big Data' folder

Second easiest way is to download the binary release.

- Fetch the latest release from here: https://github.com/Pentaho-SE-EMEA-APAC/pdi-marklogic/releases 
- Unzip the zip file
- Take the 'pdi-marklogic' folder and drop in to PENTAHO_HOME/design-tools/data-integration/plugins
- Restart PDI/Spoon
- You will find the MarkLogic steps under the 'Big Data' folder

## Samples

Sample transforms are available in the ZIP release, within the samples folder.

## Step Configuration

Create a new Database Connection in PDI - you MUST use the Wizard for this. Select MarkLogic Server as the database

- Host - The MarkLogic Server hostname
- Port - MarkLogic REST API port (use 8000 by default)
- Database - Type 'Documents', or your preferred MarkLogic database name (must already exist)
- Authentication scheme - 'digest' by default in MarkLogic Server (This field is only visible via the database Wizard interface)
- Username - access username
- Password - access password

When configuring one of the individual steps, you will type in the below fields:-

- Connection - Browse to the connection, or create one by clicking 'Wizard' (NOT 'New')
- Collection name field - Which PDI field holds the collection name
- Format field - High level MarkLogic Format - XML, JSON, TEXT, BINARY (Default: JSON)
- Content Type field - MIME type of the document (Default: application/json)
- Document URI field (optional)
- Document content field - The field containing JSON/XML for the document
- Document properties field (optional) - The field containing JSON/XML for the document's properties fragment

You will now be able to execute your transformation.

## License and Copyright

All material in this repository are Copyright 2002-2018 Hitachi Vantara. All code is licensed as Apache 2.0 unless explicitly stated. See the LICENSE file for more details. The MarkLogic Java Client API Connector included in the binary release package is also licensed as Apache 2.0, and is copyright MarkLogic Inc.

## Support Statement

This work is at Stage 1 : Development Phase: Start-up phase of an internal project. Usually a Labs experiment. (Unsupported)