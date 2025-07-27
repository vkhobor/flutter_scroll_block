import 'package:flutter/material.dart';

class ListItem {
  String appid;
  String viewid;
  bool enabled;

  ListItem({required this.appid, required this.viewid, this.enabled = true});
}

class AddItemScreen extends StatefulWidget {
  final Function(ListItem) onAdd;

  const AddItemScreen({Key? key, required this.onAdd}) : super(key: key);

  @override
  _AddItemScreenState createState() => _AddItemScreenState();
}

class _AddItemScreenState extends State<AddItemScreen> {
  final TextEditingController appidController = TextEditingController();
  final TextEditingController viewidController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Add New Item')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(
              controller: appidController,
              decoration: const InputDecoration(labelText: 'App ID'),
            ),
            TextField(
              controller: viewidController,
              decoration: const InputDecoration(labelText: 'View ID'),
            ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () {
                final newItem = ListItem(
                  appid: appidController.text,
                  viewid: viewidController.text,
                  enabled: true,
                );
                widget.onAdd(newItem);
                Navigator.pop(context);
              },
              child: const Text('Add Item'),
            ),
          ],
        ),
      ),
    );
  }
}

class InMemoryStore {
  final List<ListItem> _items = [];

  List<ListItem> get items => _items;

  void addItem(ListItem item) {
    _items.add(item);
  }

  void updateItem(int index, ListItem updatedItem) {
    if (index >= 0 && index < _items.length) {
      _items[index] = updatedItem;
    }
  }

  void deleteItem(int index) {
    if (index >= 0 && index < _items.length) {
      _items.removeAt(index);
    }
  }
}

void main() {
  runApp(const MainApp());
}

class ListScreen extends StatefulWidget {
  @override
  _ListScreenState createState() => _ListScreenState();
}

class _ListScreenState extends State<ListScreen> {
  final InMemoryStore store = InMemoryStore();

  @override
  void initState() {
    super.initState();
    _initializeData();
  }

  void _initializeData() {
    store.addItem(ListItem(appid: 'App1', viewid: 'View1', enabled: true));
    store.addItem(ListItem(appid: 'App2', viewid: 'View2', enabled: false));
  }

  @override
  Widget build(BuildContext context) {
    return Stack(children: [_buildListView(), _buildFloatingActionButton()]);
  }

  Widget _buildListView() {
    return ListView.builder(
      itemCount: store.items.length,
      itemBuilder: (context, index) {
        final item = store.items[index];
        return ListTile(
          title: Text('App ID: ${item.appid}'),
          subtitle: Text('View ID: ${item.viewid}'),
          trailing: Switch(
            value: item.enabled,
            onChanged: (value) {
              setState(() {
                store.updateItem(
                  index,
                  ListItem(
                    appid: item.appid,
                    viewid: item.viewid,
                    enabled: value,
                  ),
                );
              });
            },
          ),
        );
      },
    );
  }

  Widget _buildFloatingActionButton() {
    return Positioned(
      bottom: 16,
      right: 16,
      child: FloatingActionButton(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => AddItemScreen(
                onAdd: (newItem) {
                  setState(() {
                    store.addItem(newItem);
                  });
                },
              ),
            ),
          );
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('List Manager')),
        body: ListScreen(),
      ),
    );
  }
}
