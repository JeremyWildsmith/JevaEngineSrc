<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- JevaEngine's RPG Base dialogue transformation. -->

	<!-- Output with -->
	<xsl:output method="text" indent="no" />

	<!-- Match the first node. The first node will be the root node of a dialogue. 
		It contains metadata about the dialogue (attributes) such as the corresponding 
		script, as well as the name of the script (the TEXT of a node.) It links 
		to potential queries [that are either selected randomly or selected via the 
		entry condition.] -->
	<xsl:template match="map">
		<xsl:text>{</xsl:text>
		<xsl:for-each select="node">
			<xsl:call-template name="rootNode" />
		</xsl:for-each>
		<xsl:text>}</xsl:text>
	</xsl:template>

	<xsl:template match="arrowlink" name="link">
		<xsl:if test="position() > 1">
			<xsl:text>,</xsl:text>
		</xsl:if>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="@DESTINATION" />
		<xsl:text>"</xsl:text>
	</xsl:template>

	<xsl:template match="node" name="answer">
		<xsl:if test="position() > 1">
			<xsl:text>,</xsl:text>
		</xsl:if>
		<xsl:text>{</xsl:text>
		<xsl:text>"answer": "</xsl:text>
		<xsl:call-template name="escape-javascript">
			<xsl:with-param name="string" select="@TEXT" />
		</xsl:call-template>
		<xsl:text>"</xsl:text>

		<xsl:if test="node">
			<xsl:text>, "queries": [</xsl:text>
			<xsl:for-each select="node">
				<xsl:call-template name="query" />
			</xsl:for-each>
			<xsl:text>]</xsl:text>
		</xsl:if>

		<xsl:if test="arrowlink">
			<xsl:text>, "links": [</xsl:text>
			<xsl:for-each select="arrowlink">
				<xsl:call-template name="link" />
			</xsl:for-each>
			<xsl:text>]</xsl:text>
		</xsl:if>

		<xsl:call-template name="eval" />
		<xsl:call-template name="exec" />

		<xsl:text>}</xsl:text>
	</xsl:template>

	<xsl:template match="node" name="query">
		<xsl:if test="position() > 1">
			<xsl:text>,</xsl:text>
		</xsl:if>
		<xsl:text>{</xsl:text>
		<xsl:if test="linktarget">
			<xsl:text>"label": "</xsl:text>
			<xsl:value-of select="@ID" />
			<xsl:text>", </xsl:text>
		</xsl:if>

		<xsl:text>"query": "</xsl:text>
		<xsl:call-template name="escape-javascript">
			<xsl:with-param name="string" select="@TEXT" />
		</xsl:call-template>
		<xsl:text>", </xsl:text>


		<xsl:text>"answers": [</xsl:text>
		<xsl:for-each select="node">
			<xsl:call-template name="answer" />
		</xsl:for-each>
		<xsl:text>]</xsl:text>

		<xsl:call-template name="eval" />

		<xsl:text>}</xsl:text>
	</xsl:template>

	<xsl:template match="node" name="eval">
		<xsl:if test="attribute[@NAME='eval']">
			<xsl:text>, "eval": "</xsl:text>
			<xsl:call-template name="escape-javascript">
				<xsl:with-param name="string" select="attribute[@NAME='eval']/@VALUE" />
			</xsl:call-template>
			<xsl:text>"</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template match="node" name="exec">
		<xsl:if test="attribute[@NAME='exec']">
			<xsl:text>, "exec": "</xsl:text>
			<xsl:call-template name="escape-javascript">
				<xsl:with-param name="string" select="attribute[@NAME='exec']/@VALUE" />
			</xsl:call-template>
			<xsl:text>"</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template match="node" name="rootNode">
		<xsl:for-each select="attribute">
			<xsl:call-template name="rootAttributes" />
		</xsl:for-each>
		<xsl:text>"entrys": [</xsl:text>
		<xsl:for-each select="node">
			<xsl:call-template name="query" />
		</xsl:for-each>
		<xsl:text>],</xsl:text>

		<xsl:text>"name": "</xsl:text>
		<xsl:call-template name="escape-javascript">
			<xsl:with-param name="string" select="@TEXT" />
		</xsl:call-template>
		<xsl:text>"</xsl:text>
	</xsl:template>

	<xsl:template match="attribute" name="rootAttributes">
		<xsl:choose>
			<xsl:when test="@NAME = 'script'">
				<xsl:text>"script": "</xsl:text>
				<xsl:call-template name="escape-javascript">
					<xsl:with-param name="string" select="@VALUE" />
				</xsl:call-template>
				<xsl:text>",</xsl:text>
			</xsl:when>
		</xsl:choose>
	</xsl:template>


	<!-- Javascript string escape template by Jeni Tennison Source: http://holytshirt.blogspot.com/2008/06/xslt-javascript-escaping.html 
		Author page: http://www.jenitennison.com/ -->
	<xsl:template name="escape-javascript">
		<xsl:param name="string" />
		<xsl:choose>

			<xsl:when test="contains($string, '&quot;')">
				<xsl:call-template name="escape-javascript">
					<xsl:with-param name="string"
						select="substring-before($string, '&quot;')" />
				</xsl:call-template>
				<xsl:text>\"</xsl:text>
				<xsl:call-template name="escape-javascript">
					<xsl:with-param name="string"
						select="substring-after($string, '&quot;')" />
				</xsl:call-template>
			</xsl:when>

			<xsl:when test="contains($string, '&#xA;')">
				<xsl:call-template name="escape-javascript">
					<xsl:with-param name="string"
						select="substring-before($string, '&#xA;')" />
				</xsl:call-template>
				<xsl:text>\n</xsl:text>
				<xsl:call-template name="escape-javascript">
					<xsl:with-param name="string"
						select="substring-after($string, '&#xA;')" />
				</xsl:call-template>
			</xsl:when>

			<xsl:when test="contains($string, '\')">
				<xsl:value-of select="substring-before($string, '\')" />
				<xsl:text>\\</xsl:text>
				<xsl:call-template name="escape-javascript">
					<xsl:with-param name="string" select="substring-after($string, '\')" />
				</xsl:call-template>
			</xsl:when>

			<xsl:otherwise>
				<xsl:value-of select="$string" />
			</xsl:otherwise>

		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
