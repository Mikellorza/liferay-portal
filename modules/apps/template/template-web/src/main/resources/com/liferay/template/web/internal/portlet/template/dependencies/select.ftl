<#include "init.ftl">

<#assign variableName = name + ".getKey()" />

${r"<#if"} (${variableName})??>
	${r"${"}${variableName}${r"}"}
${r"</#if>"}