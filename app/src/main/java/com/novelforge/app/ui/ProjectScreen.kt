package com.novelforge.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.novelforge.app.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ProjectScreen(book: Book, entries: List<EntrySummary>, onOpen:(Long)->Unit, onRead:(Long)->Unit, onAdd:(String)->Unit,onEdit:()->Unit,onExport:(Boolean)->Unit,onMove:(Long,Int)->Unit,onStatus:(Long,String)->Unit,onArc:(Long,Long?)->Unit) {
    var tab by rememberSaveable(book.id) { mutableIntStateOf(0) }
    var filter by rememberSaveable(book.id) { mutableStateOf("All") }
    val chapters=entries.filter {it.kind=="Chapter"}; val words=chapters.sumOf {it.words}
    var arcFor by remember { mutableStateOf<EntrySummary?>(null) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(start=24.dp,end=12.dp,top=12.dp,bottom=10.dp),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Eyebrow("YOUR WORKSPACE"); Text(book.title.replace('\n',' '),style=MaterialTheme.typography.titleLarge,maxLines=1,overflow=TextOverflow.Ellipsis) }
            ActionIcon(Icons.Outlined.EditNote,"Edit book details",onEdit)
        }
        SecondaryScrollableTabRow(selectedTabIndex=tab,edgePadding=24.dp,containerColor=MaterialTheme.colorScheme.background,divider={HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)}) {
            listOf("Overview","Manuscript","Story bible","Board").forEachIndexed { i,label -> Tab(selected=tab==i,onClick={tab=i;filter="All"},text={Text(label)}) }
        }
        LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            if(tab==0) {
                item { Row(horizontalArrangement=Arrangement.spacedBy(22.dp),verticalAlignment=Alignment.CenterVertically) {
                    BookCover(book,Modifier.width(115.dp).height(165.dp))
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(10.dp)) { Eyebrow(book.genre); Text(book.title.replace('\n',' '),style=MaterialTheme.typography.headlineMedium); Text(book.author.ifBlank {"Your story is taking shape"},style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
                } }
                item { SurfaceCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Eyebrow("MANUSCRIPT PROGRESS"); Text(number(words),style=MaterialTheme.typography.headlineLarge) }; Text("of ${number(book.target)} words",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
                    Spacer(Modifier.height(12.dp)); LinearProgressIndicator(progress={(words.toFloat()/book.target).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth().height(5.dp),trackColor=MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(14.dp)); Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text("${chapters.size} chapters",style=MaterialTheme.typography.bodySmall); Text("${entries.count {it.kind=="Character"}} characters",style=MaterialTheme.typography.bodySmall); Text("${entries.count {it.kind=="Arc"}} arcs",style=MaterialTheme.typography.bodySmall) }
                } }
                item { Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Button(onClick={chapters.maxByOrNull {it.updated}?.let {onOpen(it.id)} ?: onAdd("Chapter")},modifier=Modifier.weight(1f),shape=RoundedCornerShape(12.dp),contentPadding=PaddingValues(14.dp)) { Icon(Icons.Outlined.Edit,null,Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)); Text("Write") }
                    OutlinedButton(onClick={chapters.firstOrNull()?.let {onRead(it.id)}},modifier=Modifier.weight(1f),enabled=chapters.isNotEmpty(),shape=RoundedCornerShape(12.dp),contentPadding=PaddingValues(14.dp)) { Icon(Icons.Outlined.MenuBook,null,Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)); Text("Read") }
                } }
                if(book.synopsis.isNotBlank()) item { Column(verticalArrangement=Arrangement.spacedBy(8.dp)) { Eyebrow("THE STORY SO FAR"); Text(book.synopsis,style=MaterialTheme.typography.bodyLarge) } }
                item { SurfaceCard(Modifier.fillMaxWidth()) { Row(verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Outlined.Lightbulb,null,tint=MaterialTheme.colorScheme.secondary); Spacer(Modifier.width(10.dp)); Eyebrow("A SMALL SPARK") }; Spacer(Modifier.height(12.dp)); Text("What does your character want—and what are they afraid to lose?",style=MaterialTheme.typography.titleLarge); Spacer(Modifier.height(8.dp)); Text("A strong scene changes something. Give your character a goal, an obstacle, and a choice.",color=MaterialTheme.colorScheme.onSurfaceVariant); TextButton(onClick={onAdd("Plot thread")}) { Text("Capture an idea"); Spacer(Modifier.width(8.dp)); Icon(Icons.AutoMirrored.Outlined.ArrowForward,null,Modifier.size(16.dp)) } } }
                item { SectionTitle("Recent pages") }
                items(entries.sortedByDescending {it.updated}.take(3),key={it.id}) { EntryRow(it,onClick={onOpen(it.id)}) }
                item { Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) { OutlinedButton(onClick={onExport(false)},Modifier.weight(1f)) {Icon(Icons.Outlined.Backup,null,Modifier.size(17.dp)); Spacer(Modifier.width(6.dp)); Text("Backup")}; OutlinedButton(onClick={onExport(true)},Modifier.weight(1f)) {Icon(Icons.Outlined.FileUpload,null,Modifier.size(17.dp)); Spacer(Modifier.width(6.dp)); Text("Export .md")} } }
            }
            if(tab==1) {
                item { SectionTitle("Shape the story", "${chapters.size} chapters · ${number(words)} words", "Add chapter",{onAdd("Chapter")}) }
                item { Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) { FilterChip(selected=filter=="All",onClick={filter="All"},label={Text("All chapters")}); entries.filter {it.kind=="Arc"}.forEach { arc -> FilterChip(selected=filter==arc.id.toString(),onClick={filter=arc.id.toString()},label={Text(arc.title)}) }; AssistChip(onClick={onAdd("Arc")},label={Text("New arc")},leadingIcon={Icon(Icons.Outlined.Add,null,Modifier.size(15.dp))}) } }
                val visible=chapters.filter {filter=="All" || it.parentId?.toString()==filter}
                if(visible.isEmpty()) item { EmptyState("The next page is yours", "Add a chapter to begin your manuscript.",action="Add chapter",onAction={onAdd("Chapter")}) }
                items(visible,key={it.id}) { e ->
                    var menu by remember { mutableStateOf(false) }
                    SurfaceCard(Modifier.fillMaxWidth()) { Row(verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).clickable {onOpen(e.id)},verticalArrangement=Arrangement.spacedBy(8.dp)) { Eyebrow("CHAPTER ${chapters.indexOf(e)+1}" + (entries.find {it.id==e.parentId}?.let { " · ${it.title}" } ?: "")); Text(e.title,style=MaterialTheme.typography.titleLarge); Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) { StatusPill(e.status); Text("${number(e.words)} words",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) } }
                        Box { ActionIcon(Icons.Outlined.MoreVert,"Chapter actions",{menu=true}); DropdownMenu(expanded=menu,onDismissRequest={menu=false}) {
                            DropdownMenuItem(text={Text("Read chapter")},onClick={menu=false;onRead(e.id)})
                            DropdownMenuItem(text={Text("Move up")},onClick={menu=false;onMove(e.id,-1)},enabled=chapters.indexOf(e)>0)
                            DropdownMenuItem(text={Text("Move down")},onClick={menu=false;onMove(e.id,1)},enabled=chapters.indexOf(e)<chapters.lastIndex)
                            DropdownMenuItem(text={Text("Assign arc")},onClick={menu=false;arcFor=e})
                        } }
                    } }
                }
            }
            if(tab==2) {
                item { SectionTitle("Every detail, connected", "The people, places, and rules of your world", "Add note",{onAdd("Note")}) }
                item { Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("All","Character","Location","Arc","Backstory","Plot thread","Worldbuilding","Screenplay","Research","Note").forEach { kind -> FilterChip(selected=filter==kind,onClick={filter=kind},label={Text(kind)}) } } }
                val visible=entries.filter {it.kind!="Chapter" && (filter=="All" || it.kind==filter)}
                if(visible.isEmpty()) item { EmptyState("Build a world worth getting lost in", "Start with a character, a place, or an unanswered question.",Icons.Outlined.Public,"Add a character",{onAdd("Character")}) }
                items(visible,key={it.id}) { e -> EntryRow(e,onClick={onOpen(e.id)}) }
            }
            if(tab==3) {
                item { SectionTitle("From first draft to final", "Move chapters through your writing process") }
                statuses.forEach { status ->
                    item { Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) { StatusPill(status); Text("${chapters.count {it.status==status}} chapters",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) } }
                    val group=chapters.filter {it.status==status}
                    if(group.isEmpty()) item { Text("No chapters here yet",Modifier.fillMaxWidth().border(1.dp,MaterialTheme.colorScheme.outlineVariant,RoundedCornerShape(12.dp)).padding(18.dp),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(group,key={it.id}) { e -> var expanded by remember {mutableStateOf(false)}
                        SurfaceCard(Modifier.fillMaxWidth()) { Row(verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f).clickable {onOpen(e.id)}) {Text(e.title,style=MaterialTheme.typography.titleMedium); Text("${number(e.words)} words",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}; Box {ActionIcon(Icons.Outlined.SwapHoriz,"Change drafting status",{expanded=true}); DropdownMenu(expanded=expanded,onDismissRequest={expanded=false}) {statuses.forEach {s->DropdownMenuItem(text={Text(s)},onClick={expanded=false;onStatus(e.id,s)})}}} } }
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
    arcFor?.let { e -> AlertDialog(onDismissRequest={arcFor=null},title={Text("Assign chapter to arc")},text={Column { TextButton(onClick={onArc(e.id,null);arcFor=null}) {Text("No arc")}; entries.filter {it.kind=="Arc"}.forEach {arc->TextButton(onClick={onArc(e.id,arc.id);arcFor=null}) {Text(arc.title)}} }},confirmButton={TextButton(onClick={arcFor=null}) {Text("Cancel")}}) }
}
@Composable fun EntryRow(e: EntrySummary,onClick:()->Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick=onClick).padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
        Box(Modifier.size(46.dp).background(MaterialTheme.colorScheme.primaryContainer,RoundedCornerShape(13.dp)),contentAlignment=Alignment.Center) {Icon(kindIcon(e.kind),null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(23.dp))}
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) {Text(e.title,style=MaterialTheme.typography.titleMedium,maxLines=2,overflow=TextOverflow.Ellipsis); Text("${e.kind} · ${number(e.words)} words",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
        Icon(Icons.Outlined.ChevronRight,null,tint=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
