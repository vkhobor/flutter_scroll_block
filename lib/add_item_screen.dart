import 'package:flutter/material.dart';
import 'package:flutter_scroll_block/settings_store.dart';

class AddItemScreen extends StatefulWidget {
  final Function(ListItem) onAdd;

  const AddItemScreen({Key? key, required this.onAdd}) : super(key: key);

  @override
  _AddItemScreenState createState() => _AddItemScreenState();
}

class _AddItemScreenState extends State<AddItemScreen> {
  final TextEditingController appidController = TextEditingController();
  final TextEditingController viewidController = TextEditingController();

  bool usePolling = false;
  bool immediatelyBlock = false;

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
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Use Polling'),
                Switch(
                  value: usePolling,
                  onChanged: (value) {
                    setState(() {
                      usePolling = value;
                    });
                  },
                ),
              ],
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Immediately Block'),
                Switch(
                  value: immediatelyBlock,
                  onChanged: (value) {
                    setState(() {
                      immediatelyBlock = value;
                    });
                  },
                ),
              ],
            ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () {
                final newItem = ListItem(
                  appid: appidController.text,
                  viewid: viewidController.text,
                  enabled: true,
                  usePolling: usePolling,
                  immediatelyBlock: immediatelyBlock,
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
