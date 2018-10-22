<?xml version="1.0"?>
<!--
  ~ Copyright (c) 2018 CA. All rights reserved.
  ~ This software may be modified and distributed under the terms
  ~ of the MIT license.  See the LICENSE file for details.
  -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:strip-space elements="*" />
    <xsl:output method="xml" indent="yes" />

    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*" />
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>