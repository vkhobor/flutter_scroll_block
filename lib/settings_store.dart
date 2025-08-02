import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';

const String storageKey = 'list_items';

class SettingStore extends ChangeNotifier {
  final SharedPreferencesAsync prefs;
  final List<ListItem> _items = [];
  static const platform = MethodChannel(
    'com.example.flutter_scroll_block/settings',
  );

  SettingStore(this.prefs);

  Future<void> handleOnChange(MethodCall call) async {
    if (call.method == 'onChange') {
      print("🌸 Flutter received: ${call.arguments}");
      await _loadFromPreferences();
      notifyListeners();
    }
  }

  void setupChannel() {
    platform.setMethodCallHandler((call) async {
      await handleOnChange(call);
    });
  }

  Future<void> init() async {
    await _loadFromPreferences();
    notifyListeners();
    setupChannel();
  }

  Future<void> _loadFromPreferences() async {
    final String? jsonString = await prefs.getString(storageKey);
    if (jsonString != null) {
      final List<dynamic> jsonList = json.decode(jsonString);
      _items.clear();
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
    notifyListeners();
  }

  Future<void> updateItem(int index, ListItem updatedItem) async {
    if (index >= 0 && index < _items.length) {
      _items[index] = updatedItem;
    }
    await _saveToPreferences();
    notifyListeners();
  }

  Future<void> disableAll() async {
    for (var item in _items) {
      item.enabled = false;
    }
    await _saveToPreferences();
    notifyListeners();
  }

  Future<void> enableAll() async {
    for (var item in _items) {
      item.enabled = true;
    }
    await _saveToPreferences();
    notifyListeners();
  }

  Future<void> deleteItem(int index) async {
    if (index >= 0 && index < _items.length) {
      _items.removeAt(index);
    }
    await _saveToPreferences();
    notifyListeners();
  }
}

class ListItem {
  String appid;
  String viewid;
  bool enabled;
  bool usePolling;

  ListItem({
    required this.appid,
    required this.viewid,
    this.enabled = true,
    this.usePolling = false,
  });

  Map<String, dynamic> toJson() {
    return {
      'appid': appid,
      'viewid': viewid,
      'enabled': enabled,
      'usePolling': usePolling,
    };
  }

  factory ListItem.fromJson(Map<String, dynamic> json) {
    return ListItem(
      appid: json['appid'],
      viewid: json['viewid'],
      enabled: json['enabled'],
      usePolling: false,
    );
  }
}
