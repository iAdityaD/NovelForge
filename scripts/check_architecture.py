"""Enforce architectural dependency direction without another build plugin."""
from pathlib import Path
import re
errors=[]
for build in Path('.').glob('*/*/build.gradle.kts'):
 module=str(build.parent)
 deps=re.findall(r'project\("(:[^\"]+)"\)',build.read_text())
 if module.startswith('domain/'):
  for dep in deps:
   if not dep.startswith((':domain:', ':core:model')): errors.append(f'{module}: forbidden dependency {dep}')
  for source in build.parent.rglob('*.kt'):
   if re.search(r'^import (android\.|androidx\.|com\.novelforge\.app\.data|com\.novelforge\.data\.|com\.novelforge\.presentation)',source.read_text(),re.M):errors.append(f'{source}: platform/data/presentation import in domain')
 if module.startswith('feature/'):
  for dep in deps:
   if dep.startswith((':feature:',':data:',':core:database',':app')):errors.append(f'{module}: forbidden dependency {dep}')
  for source in build.parent.rglob('*.kt'):
   if re.search(r'^import (androidx\.room|com\.novelforge\.app\.data|com\.novelforge\.data\.)',source.read_text(),re.M):errors.append(f'{source}: storage implementation import in feature')
assert not errors,'\n'.join(errors)
print('Architecture boundaries passed')
