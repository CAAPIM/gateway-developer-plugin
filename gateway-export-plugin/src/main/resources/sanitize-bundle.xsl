<!--
  ~ Copyright (c) 2018 CA. All rights reserved.
  ~ This software may be modified and distributed under the terms
  ~ of the MIT license.  See the LICENSE file for details.
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:l7="http://ns.l7tech.com/2010/04/gateway-management"
                version="1.0">
    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*" />
        </xsl:copy>
    </xsl:template>

    <!-- template for the document element -->
    <xsl:template match="/l7:Item|/l7:Item/l7:Resource">
        <xsl:apply-templates select="node()" />
    </xsl:template>
    <!-- remove the root elements-->
    <xsl:template match="/l7:Item/l7:Name|/l7:Item/l7:Type|/l7:Item/l7:TimeStamp|/l7:Item/l7:Link" />
    <xsl:template match="
    l7:TimeStamp|
    l7:Bundle/l7:References/l7:Item/l7:Resource/*/@version|
    l7:Bundle/l7:References/l7:Item/l7:Resource/l7:Service/l7:ServiceDetail/@version|
    l7:Bundle/l7:References/l7:Item/l7:Resource/l7:Service/l7:Resources/l7:ResourceSet/l7:Resource/@version|
    l7:Bundle/l7:References/l7:Item/l7:Resource//l7:Property[@key='revision']|
    l7:Bundle/l7:References/l7:Item/l7:Resource//l7:Property[@key='policyRevision']|
    l7:Bundle/l7:Mappings/l7:Mapping/@srcUri" />
</xsl:stylesheet>