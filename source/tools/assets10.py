from pathlib import Path
from PIL import Image,ImageDraw
import json
r=Path(__file__).resolve().parents[1]/'src/main/resources'
a=r/'assets/ng_verkehr';data=r/'data/ng_verkehr'
(a/'textures/item').mkdir(parents=True,exist_ok=True)
names={
'charger':('E-Ladesäule','EV charger','⚡'), 'charge_control':('Ladepark-Steuerung','Charging park controller','EV'),
'fuel_pump':('Tanksäule','Fuel pump','F'), 'fuel_cashier':('Tankstellen-Kasse','Fuel station cashier','K'),
'wash_entry':('Waschanlagen-Einfahrt','Car wash entry','W'), 'wash_tunnel':('Waschtunnel','Wash tunnel','W'),
'road_bollard':('Versenkbarer Straßenpoller','Retractable road bollard','!'), 'card_reader':('Zufahrtskarten-Leser','Access card reader','ID'),
'plate_reader':('Kennzeichen-Erkennung','License plate reader','NG'), 'delivery_zone':('Lieferzonen-Steuerung','Delivery zone controller','L'),
'bus_display':('Haltestellenanzeige','Bus stop display','BUS'), 'bus_route':('Buslinien-Verwaltung','Bus route controller','BUS'),
'bus_sensor':('Bus-Ankunftssensor','Bus arrival sensor','BUS'), 'school_warning':('Schulweg-Warnung','School crossing warning','!'),
'work_exit':('Baustellen-Ausfahrtswarnung','Worksite exit warning','!'), 'weather_warning':('Wetter-Warnanlage','Weather warning','!'),
'visibility_control':('Sichtwarn-Steuerung','Visibility warning controller','!'), 'tunnel_call':('Tunnel-Notruf','Tunnel emergency call','SOS'),
'escape_sign':('Fluchtweg-Anzeige','Escape route sign','EXIT'), 'operations_desk':('Verkehrsleitstelle','Traffic operations desk','NG')}
def write(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
def box(fr,to,tex):return {'from':fr,'to':to,'faces':{d:{'texture':tex} for d in ['north','south','east','west','up','down']}}
for idx,(key,(de,en,icon)) in enumerate(names.items()):
 ident='ops_'+key
 for state in range(4):
  img=Image.new('RGB',(32,32),(25,34,45));d=ImageDraw.Draw(img);d.rectangle((1,1,30,30),outline=(80,100,120),width=2);d.rectangle((4,4,27,22),fill=(4,12,19));color=[(60,180,240),(50,240,110),(255,80,40),(200,140,40)][state];d.rectangle((5,25,26,28),fill=color)
  text=icon if icon.isascii() else 'EV';d.text((max(5,16-len(text)*3),8),text,fill=color)
  if key=='road_bollard':
   for y in [6,18]:d.rectangle((0,y,31,y+5),fill=(235,225,215))
  if key=='escape_sign':d.rectangle((3,3,28,22),fill=(10,100,40));d.text((4,8),'EXIT',fill='white')
  if key=='tunnel_call':d.rectangle((4,4,27,22),fill=(190,45,20));d.text((6,8),'SOS',fill='white')
  p=a/f'textures/block/{ident}_{state}.png';p.parent.mkdir(parents=True,exist_ok=True);img.save(p)
  elements=[box([2,0,2],[14,16,14],'#all')]
  if key=='road_bollard':elements=[box([4,0,4],[12,2 if state==1 else 16,12],'#all')]
  elif key=='bus_sensor':elements=[box([0,0,0],[16,1,16],'#all')]
  elif key=='wash_tunnel':
   elements=[box([-16,0,0],[-12,32,16],'#all'),box([28,0,0],[32,32,16],'#all')]
   if state==1:elements += [box([-12,2,3],[-8,30,13],'#brush'),box([24,2,3],[28,30,13],'#brush')]
  elif key in ('bus_display','escape_sign','school_warning','work_exit','weather_warning','visibility_control'):elements=[box([1,7,4],[15,16,12],'#all'),box([7,0,7],[9,7,9],'#all')]
  elif key=='operations_desk':elements=[box([0,0,0],[16,9,16],'#all'),box([1,9,8],[15,16,12],'#all')]
  textures={'all':f'ng_verkehr:block/{ident}_{state}','particle':f'ng_verkehr:block/{ident}_{state}','brush':'ng_verkehr:block/ops_brush'}
  write(a/f'models/block/{ident}_{state}.json',{'textures':textures,'elements':elements})
 variants={}
 for facing,angle in [('north',0),('east',90),('south',180),('west',270)]:
  for state in range(4):variants[f'facing={facing},status={state}']={'model':f'ng_verkehr:block/{ident}_{state}','y':angle}
 write(a/f'blockstates/{ident}.json',{'variants':variants});write(a/f'models/item/{ident}.json',{'parent':f'ng_verkehr:block/{ident}_0'})
 write(data/f'loot_tables/blocks/{ident}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'ng_verkehr:{ident}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 write(data/f'recipes/{ident}.json',{'type':'minecraft:crafting_shaped','pattern':['III','IRI','IBI'],'key':{'I':{'item':'minecraft:iron_ingot'},'R':{'item':'minecraft:redstone'},'B':{'item':'minecraft:'+['copper_ingot','quartz','bucket','gold_ingot','oak_fence_gate','blue_wool','piston','paper','spyglass','chest','glowstone_dust','book','tripwire_hook','yellow_wool','orange_wool','water_bucket','glass','bell','green_wool','comparator'][sorted(names).index(key)]}},'result':{'item':f'ng_verkehr:{ident}'}})
# Active wash brushes scroll through four animation frames.
img=Image.new('RGB',(16,64));d=ImageDraw.Draw(img)
for frame in range(4):
 for x in range(16):d.line((x,frame*16,x,frame*16+15),fill=(30,100+((x+frame*3)%8)*15,200))
img.save(a/'textures/block/ops_brush.png');write(a/'textures/block/ops_brush.png.mcmeta',{'animation':{'frametime':2}})
for key,de,en in [('operations_linker','Betriebsgeräte-Verbinder','Operations device linker'),('access_card','Persönliche Zufahrtskarte','Personal access card')]:
 img=Image.new('RGBA',(16,16));d=ImageDraw.Draw(img);d.rectangle((2,4,13,12),fill=(35,65,90),outline=(90,205,255));d.rectangle((4,7,7,10),fill=(240,195,70));img.save(a/f'textures/item/{key}.png');write(a/f'models/item/{key}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'ng_verkehr:item/{key}'}})
 if key=='operations_linker':write(data/f'recipes/{key}.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'minecraft:iron_ingot'},{'item':'minecraft:redstone'},{'item':'minecraft:stick'}],'result':{'item':f'ng_verkehr:{key}'}})
for locale in ['de_de','en_us']:
 p=a/f'lang/{locale}.json';lang=json.loads(p.read_text())
 for key,v in names.items():lang['block.ng_verkehr.ops_'+key]=v[0 if locale=='de_de' else 1]
 lang['item.ng_verkehr.operations_linker']='Betriebsgeräte-Verbinder' if locale=='de_de' else 'Operations device linker';lang['item.ng_verkehr.access_card']='Persönliche Zufahrtskarte' if locale=='de_de' else 'Personal access card';lang['itemGroup.ng_verkehr.operations']='Betrieb und Versorgung' if locale=='de_de' else 'Operations and services';write(p,lang)
p=a/'creative_categories.json';cats=[c for c in json.loads(p.read_text()) if c['id']!='operations'];cats.append({'id':'operations','title':'Betrieb und Versorgung','icon':'ops_operations_desk','items':['ops_'+k for k in names]+['operations_linker','access_card']});write(p,cats)
