Skeletal runtime asset layout

Preferred unpacked layout:
  app/src/main/assets/skeletal_runtime/<entity_id>/rig.json
  app/src/main/assets/skeletal_runtime/<entity_id>/parts/final/*.png
  app/src/main/assets/skeletal_runtime/<entity_id>/animations/idle.json
  app/src/main/assets/skeletal_runtime/<entity_id>/animations/attack.json
  app/src/main/assets/skeletal_runtime/<entity_id>/animations/hit.json
  app/src/main/assets/skeletal_runtime/<entity_id>/animations/death.json

The runtime also supports a single asset ZIP:
  app/src/main/assets/astralforge_skeletal_runtime_v2.zip

The ZIP may contain either:
  <entity_id>/...
or:
  astralforge_skeletal_runtime_v2/<entity_id>/...

Battle falls back to the existing frame-based EntitySpriteStore whenever a skeletal rig is unavailable.
