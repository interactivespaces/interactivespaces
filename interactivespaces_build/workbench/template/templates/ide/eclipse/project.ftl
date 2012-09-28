<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
	<name>${project.activityDescription.identifyingName}</name>
	<comment/>
	<projects/>
	<natures>
	<#list natures as nature>
		<nature>${nature}</nature>
	</#list>
	</natures>
	<buildSpec>
		<buildCommand>
			<name>${builder}</name>
			<arguments/>
		</buildCommand>
	</buildSpec>
	<linkedResources/>
</projectDescription>
