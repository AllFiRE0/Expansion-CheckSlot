```markdown
# Expansion-CheckSlot

Expansion for PlaceholderAPI that provides placeholders for checking player inventory slots.

Расширение для PlaceholderAPI, предоставляющее заполнители для проверки слотов инвентаря игрока.

## Installation / Установка

1. Download `Expansion-CheckSlot.jar` from [Releases](https://github.com/AllFiRE/Expansion-CheckSlot/releases)
2. Place it in `plugins/PlaceholderAPI/expansions/`
3. Run `/papi reload` or restart the server

---

1. Скачайте `Expansion-CheckSlot.jar` из [Releases](https://github.com/AllFiRE/Expansion-CheckSlot/releases)
2. Поместите в папку `plugins/PlaceholderAPI/expansions/`
3. Выполните `/papi reload` или перезагрузите сервер

## Placeholders / Заполнители

### Syntax / Синтаксис

```text
%checkslot_<type><slot><fallback>%
%checkslot_<player><type><slot><fallback>%
%checkslot{placeholder}<type><slot>_<fallback>%
```

Slots / Слоты

Slot / Слот Description / Описание
0-8 Hotbar / Хотбар
9-35 Main inventory / Основной инвентарь
36 Boots / Ботинки
37 Leggings / Поножи
38 Chestplate / Нагрудник
39 Helmet / Шлем
40 Offhand / Вторая рука
helmet Helmet / Шлем
chestplate Chestplate / Нагрудник
leggings Leggings / Поножи
boots Boots / Ботинки
offhand Offhand / Вторая рука

Types / Типы

Item Name / Название предмета

Placeholder Language / Язык Example / Пример
name English / Английский Calcite
rawname English lowercase / Английский нижний регистр calcite
displayname Client language / Язык клиента Кальцит
rawdisplayname Client language no colors / Язык клиента без цветов Кальцит

name and rawname are for checks in other plugins:
name и rawname для проверок в других плагинах:

```text
%checkslot_rawname_1_no% == calcite
```

Lore / Описание

Placeholder Description / Описание Example / Пример
lore All lines with \n / Все строки через \n line1\nline2\nline3
lore-1 ... lore-N Single line / Одна строка line1
lore_1,2,3 Selected lines with delimiter / Выбранные строки с разделителем line1, line2, line3
lore_1&#124;2&#124;3 Same with &#124; / То же с &#124; line1&#124;line2&#124;line3
lore_1-5 Line range / Диапазон строк line1, line2, line3, line4, line5
lore_1\n2\n3 Selected lines with newline / Выбранные строки с переносом line1\nline2\nline3

Delimiter between numbers = delimiter in output:
Разделитель между номерами = разделитель в выводе:

```text
%checkslot_lore_1,2,3_0_&fEmpty%  →  строка1, строка2, строка3
%checkslot_lore_1|2|3_0_&fEmpty%  →  строка1|строка2|строка3
%checkslot_lore_1-5_0_&fEmpty%    →  строка1, строка2, ..., строка5
```

Enchantments / Зачарования

Placeholder Language / Язык Format / Формат Example / Пример
enchants English / Английский Formatted / Формат. Sharpness V, Unbreaking III
rawenchants English / Английский Raw / Сырой sharpness5,unbreaking3
displayenchants Client / Клиента Formatted / Формат. Острота V, Прочность III
rawdisplayenchants Client / Клиента Raw no colors / Сырой без цветов Острота5,Прочность3

Potion Effects / Эффекты зелий

Placeholder Language / Язык Format / Формат Example / Пример
potion English / Английский Formatted / Формат. Speed II, Strength I
rawpotion English / Английский Raw / Сырой speed2,strength1
displaypotion Client / Клиента Formatted / Формат. Скорость II, Сила I
rawdisplaypotion Client / Клиента Raw no colors / Сырой без цветов Скорость2,Сила1

Attributes / Атрибуты

Placeholder Format / Формат Example / Пример
attribute Formatted / Формат. Attack Damage +7.0 add
rawattribute Raw / Сырой attack_damage:7.0:add_number

Durability / Прочность

Placeholder Description / Описание
durability Current / Текущая
maxdurability Maximum / Максимальная

Data / Данные

Placeholder Description / Описание
data Type, amount, custom model data, etc. / Тип, количество, модель и т.д.

Player argument / Аргумент игрока

Current player / Текущий игрок:

```text
%checkslot_name_0_&fEmpty%
```

Specific player / Конкретный игрок:

```text
%checkslot_Notch_name_0_&fEmpty%
```

From placeholder / Из заполнителя:

```text
%checkslot_{player_name}name_0&fEmpty%
%checkslot_{cmi_player_name}enchants_helmet&7None%
```

Fallback with placeholders / Fallback с заполнителями

Fallback supports {placeholder} inside:
Fallback поддерживает {placeholder} внутри:

```text
%checkslot_name_0_{player_name} has nothing here%
%checkslot_Notch_name_0_Player {player_name} is empty%
```

Examples / Примеры

Check if slot 1 is calcite
Проверить, кальцит ли в 1 слоте:

```text
%checkslot_rawname_1_no% == calcite
```

Show item name in slot 0
Показать название предмета в 0 слоте:

```text
%checkslot_displayname_0_&fПусто%
```

Show helmet enchantments
Показать зачарования шлема:

```text
%checkslot_displayenchants_helmet_&7Нет чар%
```

Show first 3 lore lines separated by &#124;
Показать первые 3 строки описания через &#124;:

```text
%checkslot_lore_1|2|3_0_&fПусто%
```

Menu usage / Использование в меню:

```yaml
Lore:
  - "%checkslot_lore-1_1_&fПусто%"
  - "%checkslot_lore-2_1_&fПусто%"
  - "%checkslot_lore-3_1_&fПусто%"
```

Requirements / Требования

· PlaceholderAPI 2.11.0+
· Spigot/Paper 1.16.5+ (for client translations / для клиентского перевода)
  · Falls back to English on older versions / Откат на английский на старых версиях

Author / Автор

AllFiRE

```
