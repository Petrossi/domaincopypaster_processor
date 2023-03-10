<?xml version="1.0"?>
<!--
  Copyright 2004 Guy Van den Broeck

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>

    <xsl:template match="/">
        <html>
            <head></head>
            <body>
                <xsl:apply-templates select="*[local-name(.) = 'html']/*[local-name(.) = 'body']/node()"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="*">
        <xsl:element name="{local-name()}">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*">
        <xsl:attribute name="{local-name()}">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="*[local-name(.) = 'script']">
        <!-- remove script tags ! -->
    </xsl:template>

    <xsl:template match="*[local-name(.) = 'noscript']">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="@*[starts-with(., 'javascript:')]">
        <!-- remove script attribute ! -->
    </xsl:template>

    <xsl:template match="@*[starts-with(local-name(.), 'on')]">
        <!-- remove script attribute ! -->
    </xsl:template>

</xsl:stylesheet>
