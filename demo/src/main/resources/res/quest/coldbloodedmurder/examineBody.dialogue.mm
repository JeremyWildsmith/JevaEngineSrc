<map version="1.0.1">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1409003515915" ID="ID_719297266" MODIFIED="1409738447358" TEXT="Examine Body">
<font NAME="SansSerif" SIZE="11"/>
<attribute_layout NAME_WIDTH="33" VALUE_WIDTH="233"/>
<attribute NAME="script" VALUE="quest/coldbloodedmurder/examineBody.js"/>
<node CREATED="1409038389989" ID="ID_1347607880" MODIFIED="1409649001915" POSITION="right" TEXT="You begin studying the corpse. Your eyes ponder across the body, searching for anything that catches your attention. ">
<linktarget COLOR="#b0b0b0" DESTINATION="ID_1347607880" ENDARROW="Default" ENDINCLINATION="1004;0;" ID="Arrow_ID_163345319" SOURCE="ID_1498932060" STARTARROW="None" STARTINCLINATION="1004;0;"/>
<linktarget COLOR="#b0b0b0" DESTINATION="ID_1347607880" ENDARROW="Default" ENDINCLINATION="1017;0;" ID="Arrow_ID_71599807" SOURCE="ID_1291116269" STARTARROW="None" STARTINCLINATION="1017;0;"/>
<linktarget COLOR="#b0b0b0" DESTINATION="ID_1347607880" ENDARROW="Default" ENDINCLINATION="1006;0;" ID="Arrow_ID_823114448" SOURCE="ID_1468964559" STARTARROW="None" STARTINCLINATION="1006;0;"/>
<linktarget COLOR="#b0b0b0" DESTINATION="ID_1347607880" ENDARROW="Default" ENDINCLINATION="1001;0;" ID="Arrow_ID_1833626591" SOURCE="ID_14483773" STARTARROW="None" STARTINCLINATION="839;-110;"/>
<node CREATED="1409032474344" ID="ID_733050765" MODIFIED="1409385299574" TEXT="Examine torso">
<attribute_layout NAME_WIDTH="56"/>
<attribute NAME="eval" VALUE="hasTorso()"/>
<node CREATED="1409032563179" HGAP="47" ID="ID_1601409918" MODIFIED="1409384791192" TEXT="The torso was covered in (what you can safely assume is) the subject&apos;s blood. You figure it as likely residue from the beheading or castration the body had endured. You also take note of a particular scar slashed across the subject&apos;s chest. It appears to be a deep cut that must have taken place at least a few weeks before the murder." VSHIFT="-18">
<node CREATED="1409032948095" ID="ID_1498932060" MODIFIED="1409647956059" TEXT="Okay">
<arrowlink COLOR="#b0b0b0" DESTINATION="ID_1347607880" ENDARROW="Default" ENDINCLINATION="1004;0;" ID="Arrow_ID_163345319" STARTARROW="None" STARTINCLINATION="1004;0;"/>
<attribute_layout NAME_WIDTH="33" VALUE_WIDTH="129"/>
<attribute NAME="exec" VALUE="examineTorso()"/>
</node>
</node>
</node>
<node CREATED="1409032893046" ID="ID_1733836257" MODIFIED="1409089168445" TEXT="Leave corpse"/>
<node CREATED="1409032229885" ID="ID_1757847780" MODIFIED="1409385325267" TEXT="Examine lower body">
<attribute_layout NAME_WIDTH="33" VALUE_WIDTH="113"/>
<attribute NAME="eval" VALUE="hasLowerBody()"/>
<node CREATED="1409032285394" ID="ID_1337513267" MODIFIED="1409547226872" TEXT="You discover that there was more than just the head that was removed from this corpse. The individual was also missing their right testicle and their penis - which had also been severed from the body. The whereabouts of those severed parts remained unknown.">
<node CREATED="1409032440332" ID="ID_1291116269" MODIFIED="1409647979884" TEXT="Okay">
<arrowlink DESTINATION="ID_1347607880" ENDARROW="Default" ENDINCLINATION="1017;0;" ID="Arrow_ID_71599807" STARTARROW="None" STARTINCLINATION="1017;0;"/>
<attribute_layout VALUE_WIDTH="120"/>
<attribute NAME="exec" VALUE="examineLowerBody()"/>
</node>
</node>
</node>
<node CREATED="1409031521183" ID="ID_1091270232" MODIFIED="1409385356313" TEXT="Examine feet">
<attribute_layout NAME_WIDTH="33" VALUE_WIDTH="96"/>
<attribute NAME="eval" VALUE="hasFeet()"/>
<node CREATED="1409031822640" ID="ID_1274228651" MODIFIED="1409032100607" TEXT="The socks were disgusting as they had absorbed a great deal of blood and moisture from the snow. It also appeared as though a sort of mold was growing from within the sock and sprouting outwards from the sock&apos;s fabric. Through the blood, you managed to observe a blue &amp; green checkered pattern the socks seemed to have. Otherwise, they seemed fairly plain.">
<node CREATED="1409032133557" ID="ID_1468964559" MODIFIED="1409647988299" TEXT="Okay">
<arrowlink DESTINATION="ID_1347607880" ENDARROW="Default" ENDINCLINATION="1006;0;" ID="Arrow_ID_823114448" STARTARROW="None" STARTINCLINATION="1006;0;"/>
<attribute_layout VALUE_WIDTH="126"/>
<attribute NAME="exec" VALUE="examineFeet()"/>
</node>
</node>
</node>
<node CREATED="1409030480158" ID="ID_693039190" MODIFIED="1409385367480" TEXT="Examine the scene">
<attribute_layout NAME_WIDTH="35"/>
<attribute NAME="eval" VALUE="hasScene()"/>
<node CREATED="1409030664803" ID="ID_1534728891" MODIFIED="1409038961004" TEXT="The scene was not pretty. The body was in stages of decomposition and laid naked in the snow, with the exception of the pair of socks that remained on the body&apos;s feet. The body&apos;s head had been cleanly chopped off, and missing from the corpse. Locating it may be helpful in identifying the subject.">
<node CREATED="1409031627185" ID="ID_14483773" MODIFIED="1409647994536" TEXT="Okay">
<arrowlink DESTINATION="ID_1347607880" ENDARROW="Default" ENDINCLINATION="1001;0;" ID="Arrow_ID_1833626591" STARTARROW="None" STARTINCLINATION="839;-110;"/>
<attribute_layout VALUE_WIDTH="111"/>
<attribute NAME="exec" VALUE="examineScene()"/>
</node>
</node>
</node>
</node>
</node>
</map>
