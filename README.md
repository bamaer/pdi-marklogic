# pdi-marklogic
MarkLogic steps for Pentaho Data Integration.

## Included steps

- MarkLogic Document Output Step

## Future steps

- MarkLogic Document Input Step
- MarkLogic Search Input Step

## Installation

TODO description of build and import to PDI.

## Step documentation

Below are details on how to use each step after installation

### Document Output Step

This step will use batch asynchronous writing to create new documents in MarkLogic Server based on JSON or XML content and fields within a Pentaho Data Integration stream.

#### Configuration

- Server URL
- Username
- Password
- Batch commit size
- Collection name
- Format field - High level MarkLogic Format - XML, JSON, TEXT, BINARY (Default: JSON)
- Content Type field - MIME type of the document (Default: application/json)
- Document URI field (optional)
- Document content field - The field containing JSON/XML for the document
- Document properties field (optional) - The field containing JSON/XML for the document's properties fragment

## License and Copyright

All material in this repository are Copyright 2002-2018 Hitachi Vantara. All code is licensed as Apache 2.0 unless explicitly stated. See the LICENSE file for more details.

## Support Statement

This work is at Stage 1 : Development Phase: Start-up phase of an internal project. Usually a Labs experiment. (Unsupported)