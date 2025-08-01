import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

const String storageKey = 'list_items';

class InMemoryStore {
  final SharedPreferencesAsync prefs;
  final List<ListItem> _items = [];

  InMemoryStore(this.prefs);

  Future<void> init() async {
    await _loadFromPreferences();
  }

  Future<void> _loadFromPreferences() async {
    final String? jsonString = await prefs.getString(storageKey);
    if (jsonString != null) {
      final List<dynamic> jsonList = json.decode(jsonString);
      _items.addAll(jsonList.map((item) => ListItem.fromJson(item)));
    }
  }

  Future<void> _saveToPreferences() async {
    final String jsonString = json.encode(
      _items.map((item) => item.toJson()).toList(),
    );
    await prefs.setString(storageKey, jsonString);
  }

  List<ListItem> get items => _items;

  Future<void> addItem(ListItem item) async {
    _items.add(item);
    await _saveToPreferences();
  }

  Future<void> updateItem(int index, ListItem updatedItem) async {
    if (index >= 0 && index < _items.length) {
      _items[index] = updatedItem;
    }
    await _saveToPreferences();
  }

  Future<void> disableAll() async {
    for (var item in _items) {
      item.enabled = false;
    }
    await _saveToPreferences();
  }

  Future<void> enableAll() async {
    for (var item in _items) {
      item.enabled = true;
    }
    await _saveToPreferences();
  }

  Future<void> deleteItem(int index) async {
    if (index >= 0 && index < _items.length) {
      _items.removeAt(index);
    }
    await _saveToPreferences();
  }
}

class ListItem {
  String appid;
  String viewid;
  bool enabled;

  ListItem({required this.appid, required this.viewid, this.enabled = true});

  Map<String, dynamic> toJson() {
    return {'appid': appid, 'viewid': viewid, 'enabled': enabled};
  }

  factory ListItem.fromJson(Map<String, dynamic> json) {
    return ListItem(
      appid: json['appid'],
      viewid: json['viewid'],
      enabled: json['enabled'],
    );
  }
}
