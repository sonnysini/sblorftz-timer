import 'package:flutter/material.dart';
import '../services/structure_service.dart';
import '../widgets/structure_card.dart';
import 'custom_placeholder_screen.dart';

class StructureSelectorScreen extends StatefulWidget {
  const StructureSelectorScreen({super.key});

  @override
  State<StructureSelectorScreen> createState() => _StructureSelectorScreenState();
}

class _StructureSelectorScreenState extends State<StructureSelectorScreen> {
  final StructureService _structureService = StructureService();
  late final List<FocusNode> _focusNodes;

  String _selectedType = StructureService.typeStandard;
  int _customLevelCount = 0;
  int _customTotalMinutes = 0;
  String _customStartingBlinds = '0/0';
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    // Inizializza i FocusNode in initState invece che nel campo
    _focusNodes = List.generate(3, (_) => FocusNode());
    // Carica i dati dopo l'inizializzazione
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
    try {
      final selectedType = await _structureService.getSelectedType();
      final customStructure = await _structureService.getCustomStructure();

      if (mounted) {
        setState(() {
          _selectedType = selectedType;
          _customLevelCount = customStructure.length;
          _customTotalMinutes = _structureService.getTotalDuration(customStructure);
          _customStartingBlinds = _structureService.getStartingBlinds(customStructure);
          _isLoading = false;
        });

        // Richiedi il focus dopo che tutto è caricato (importante per Fire TV)
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted && _focusNodes[0].canRequestFocus) {
            _focusNodes[0].requestFocus();
          }
        });
      }
    } catch (e) {
      // Gestione errori per evitare crash
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
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
    await Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => const CustomPlaceholderScreen(),
      ),
    );
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
                child: _isLoading
                    ? const Center(
                        child: CircularProgressIndicator(),
                      )
                    : Center(
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
                          autofocus: false,
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
