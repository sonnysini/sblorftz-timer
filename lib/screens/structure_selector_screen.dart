import 'package:flutter/material.dart';
import '../services/structure_service.dart';
import '../widgets/structure_card.dart';
import 'custom_editor_screen.dart';

class StructureSelectorScreen extends StatefulWidget {
  const StructureSelectorScreen({super.key});

  @override
  State<StructureSelectorScreen> createState() => _StructureSelectorScreenState();
}

class _StructureSelectorScreenState extends State<StructureSelectorScreen> {
  final StructureService _structureService = StructureService();
  final List<FocusNode> _focusNodes = List.generate(3, (_) => FocusNode());

  String _selectedType = StructureService.typeStandard;
  int _customLevelCount = 0;
  int _customTotalMinutes = 0;
  String _customStartingBlinds = '0/0';

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  @override
  void dispose() {
    for (var node in _focusNodes) {
      node.dispose();
    }
    super.dispose();
  }

  Future<void> _loadData() async {
    final selectedType = await _structureService.getSelectedType();
    final customStructure = await _structureService.getCustomStructure();

    setState(() {
      _selectedType = selectedType;
      _customLevelCount = customStructure.length;
      _customTotalMinutes = _structureService.getTotalDuration(customStructure);
      _customStartingBlinds = _structureService.getStartingBlinds(customStructure);
    });
  }

  Future<void> _selectStructure(String type) async {
    // Non permettere di selezionare CUSTOM se è vuota
    if (type == StructureService.typeCustom && _customLevelCount == 0) {
      _showMessage('Crea almeno un livello prima di selezionare CUSTOM');
      return;
    }

    await _structureService.setSelectedType(type);
    setState(() {
      _selectedType = type;
    });
    _showMessage('Struttura $_selectedType selezionata');
  }

  void _showMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        duration: const Duration(seconds: 2),
      ),
    );
  }

  Future<void> _openCustomEditor() async {
    final result = await Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => const CustomEditorScreen(),
      ),
    );

    // Ricarica i dati se l'editor ha salvato
    if (result == true) {
      await _loadData();
    }
  }


  String _getCustomDescription() {
    if (_customLevelCount == 0) {
      return '0 livelli · Premi OK per creare';
    }
    return '$_customLevelCount livelli · $_customStartingBlinds start · $_customTotalMinutes min';
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      body: SafeArea(
          child: Column(
            children: [
              // Header
              Padding(
                padding: const EdgeInsets.all(24),
                child: Row(
                  children: [
                    IconButton(
                      icon: const Icon(Icons.arrow_back),
                      onPressed: () => Navigator.of(context).pop(),
                    ),
                    const Spacer(),
                    Text(
                      'SCEGLI STRUTTURA',
                      style: theme.textTheme.headlineSmall,
                    ),
                    const Spacer(),
                    const SizedBox(width: 48), // Bilanciamento
                  ],
                ),
              ),
              const SizedBox(height: 24),
              // Cards
              Expanded(
                child: Center(
                  child: Container(
                    constraints: const BoxConstraints(maxWidth: 800),
                    padding: const EdgeInsets.symmetric(horizontal: 32),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        // STANDARD
                        StructureCard(
                          icon: '★',
                          title: 'STANDARD',
                          description: '18 livelli · 100/200 start · 15-10 min',
                          isSelected: _selectedType == StructureService.typeStandard,
                          focusNode: _focusNodes[0],
                          autofocus: true,
                          onPressed: () => _selectStructure(StructureService.typeStandard),
                        ),
                        const SizedBox(height: 16),
                        // VELOCE
                        StructureCard(
                          icon: '⚡',
                          title: 'VELOCE',
                          description: '18 livelli · 100/200 start · 10 min',
                          isSelected: _selectedType == StructureService.typeVeloce,
                          focusNode: _focusNodes[1],
                          onPressed: () => _selectStructure(StructureService.typeVeloce),
                        ),
                        const SizedBox(height: 16),
                        // CUSTOM
                        StructureCard(
                          icon: '✎',
                          title: 'CUSTOM',
                          description: _getCustomDescription(),
                          isSelected: _selectedType == StructureService.typeCustom,
                          focusNode: _focusNodes[2],
                          onPressed: _openCustomEditor,
                        ),
                        const SizedBox(height: 32),
                        Text(
                          'La struttura selezionata verrà\nusata per il prossimo torneo',
                          style: theme.textTheme.bodyMedium?.copyWith(
                            color: theme.colorScheme.onSurface.withOpacity(0.7),
                          ),
                          textAlign: TextAlign.center,
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
    );
  }
}
