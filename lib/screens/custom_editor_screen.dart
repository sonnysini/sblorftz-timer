import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../models/blind_level.dart';
import '../services/structure_service.dart';
import '../widgets/level_row.dart';
import '../widgets/spinner_input_popup.dart';
import '../widgets/focusable_button.dart';
import '../widgets/unsaved_changes_popup.dart';

class CustomEditorScreen extends StatefulWidget {
  const CustomEditorScreen({super.key});

  @override
  State<CustomEditorScreen> createState() => _CustomEditorScreenState();
}

class _CustomEditorScreenState extends State<CustomEditorScreen> {
  final StructureService _structureService = StructureService();
  List<BlindLevel> _levels = [];
  List<BlindLevel> _originalLevels = [];
  bool _hasUnsavedChanges = false;

  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _loadStructure();
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  Future<void> _loadStructure() async {
    final structure = await _structureService.getCustomStructure();
    setState(() {
      _levels = List.from(structure);
      _originalLevels = List.from(structure);
      _hasUnsavedChanges = false;
    });
  }

  void _markAsChanged() {
    setState(() {
      _hasUnsavedChanges = true;
    });
  }

  Future<void> _saveStructure() async {
    await _structureService.saveCustomStructure(_levels);
    setState(() {
      _originalLevels = List.from(_levels);
      _hasUnsavedChanges = false;
    });
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Struttura salvata')),
      );
      Navigator.of(context).pop(true); // Ritorna true per indicare salvataggio
    }
  }

  Future<void> _resetStructure() async {
    final confirmed = await _showConfirmDialog(
      'Eliminare tutti i livelli?',
      null,
    );

    if (confirmed == true) {
      setState(() {
        _levels.clear();
        _markAsChanged();
      });
    }
  }

  Future<void> _addLevel() async {
    // Step 1: Chiedi Small Blind
    final int? newSB = await _showSmallBlindPicker(
      isNewLevel: true,
      levelNumber: _levels.length + 1,
      currentSB: null,
    );

    if (newSB == null) return;

    // Step 2: Chiedi Durata (mostrando BB automatico)
    final int? newDuration = await _showDurationPicker(
      levelNumber: _levels.length + 1,
      smallBlind: newSB,
      currentDuration: 10,
    );

    if (newDuration == null) return;

    // Crea il livello
    final newLevel = BlindLevel(
      levelNumber: _levels.length + 1,
      smallBlind: newSB,
      bigBlind: newSB * 2,
      durationMinutes: newDuration,
    );

    setState(() {
      _levels.add(newLevel);
      _markAsChanged();
    });
  }

  Future<void> _editSmallBlind(int index) async {
    final level = _levels[index];
    final newSB = await _showSmallBlindPicker(
      isNewLevel: false,
      levelNumber: level.levelNumber,
      currentSB: level.smallBlind,
      previousSB: index > 0 ? _levels[index - 1].smallBlind : null,
      nextSB: index < _levels.length - 1 ? _levels[index + 1].smallBlind : null,
    );

    if (newSB != null && newSB != level.smallBlind) {
      setState(() {
        _levels[index] = level.copyWith(
          smallBlind: newSB,
          bigBlind: newSB * 2,
        );
        _markAsChanged();
      });
    }
  }

  Future<void> _editDuration(int index) async {
    final level = _levels[index];
    final newDuration = await _showDurationPicker(
      levelNumber: level.levelNumber,
      smallBlind: level.smallBlind,
      currentDuration: level.durationMinutes,
    );

    if (newDuration != null && newDuration != level.durationMinutes) {
      setState(() {
        _levels[index] = level.copyWith(durationMinutes: newDuration);
        _markAsChanged();
      });
    }
  }

  Future<void> _deleteLevel(int index) async {
    final level = _levels[index];
    final confirmed = await _showConfirmDialog(
      'Eliminare il livello ${level.levelNumber}?',
      '(${level.smallBlind} / ${level.bigBlind})',
    );

    if (confirmed == true) {
      setState(() {
        _levels.removeAt(index);
        // Rinumera i livelli
        for (int i = 0; i < _levels.length; i++) {
          _levels[i] = _levels[i].copyWith(levelNumber: i + 1);
        }
        _markAsChanged();
      });
    }
  }

  Future<int?> _showSmallBlindPicker({
    required bool isNewLevel,
    required int levelNumber,
    required int? currentSB,
    int? previousSB,
    int? nextSB,
  }) async {
    // Calcola valore iniziale e minimo
    int initialValue;
    int minValue;

    if (isNewLevel) {
      if (_levels.isEmpty) {
        initialValue = 100;
        minValue = 100;
      } else {
        final lastSB = _levels.last.smallBlind;
        if (lastSB < 900) {
          initialValue = lastSB + 100;
          minValue = lastSB + 100;
        } else if (lastSB == 900) {
          initialValue = 1000;
          minValue = 1000;
        } else {
          initialValue = lastSB + 500;
          minValue = lastSB + 500;
        }
      }
    } else {
      initialValue = currentSB!;
      minValue = (previousSB ?? 0) + 100;
    }

    return await showDialog<int>(
      context: context,
      barrierDismissible: false,
      builder: (context) => SpinnerInputPopup(
        title: isNewLevel ? 'NUOVO LIVELLO $levelNumber' : 'MODIFICA LIVELLO $levelNumber',
        subtitle: 'SMALL BLIND',
        initialValue: initialValue,
        formatValue: (value) => value.toString(),
        getIncrement: (current) {
          return current < 1000 ? current + 100 : current + 500;
        },
        getDecrement: (current) {
          final newValue = current < 1000 ? current - 100 : current - 500;
          return newValue < minValue ? current : newValue;
        },
        canIncrement: (current) {
          final newValue = current < 1000 ? current + 100 : current + 500;
          return nextSB == null || newValue < nextSB;
        },
        canDecrement: (current) {
          final newValue = current < 1000 ? current - 100 : current - 500;
          return newValue >= minValue;
        },
        hint: isNewLevel
            ? '▲ Incremento automatico'
            : null,
      ),
    );
  }

  Future<int?> _showDurationPicker({
    required int levelNumber,
    required int smallBlind,
    required int currentDuration,
  }) async {
    return await showDialog<int>(
      context: context,
      barrierDismissible: false,
      builder: (context) => SpinnerInputPopup(
        title: 'NUOVO LIVELLO $levelNumber',
        subtitle: 'SB: $smallBlind  →  BB: ${smallBlind * 2} ✓',
        initialValue: currentDuration,
        formatValue: (value) => '$value min',
        getIncrement: (current) => current + 1,
        getDecrement: (current) => current - 1,
        canIncrement: (current) => current < 60,
        canDecrement: (current) => current > 1,
        hint: '▲ +1 min  ▼ -1 min',
      ),
    );
  }

  Future<bool?> _showConfirmDialog(String message, String? details) async {
    return await showDialog<bool>(
      context: context,
      barrierDismissible: false,
      builder: (context) => FocusScope(
        autofocus: true,
        child: Dialog(
          child: Container(
            padding: const EdgeInsets.all(32),
            constraints: const BoxConstraints(maxWidth: 400),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  message,
                  style: Theme.of(context).textTheme.titleLarge,
                  textAlign: TextAlign.center,
                ),
                if (details != null) ...[
                  const SizedBox(height: 8),
                  Text(
                    details,
                    style: Theme.of(context).textTheme.bodyMedium,
                    textAlign: TextAlign.center,
                  ),
                ],
                const SizedBox(height: 24),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    FocusableButton(
                      text: 'NO',
                      autofocus: true,
                      onPressed: () => Navigator.of(context).pop(false),
                    ),
                    const SizedBox(width: 16),
                    FocusableButton(
                      text: 'SÌ',
                      customBackgroundColor: Colors.red,
                      onPressed: () => Navigator.of(context).pop(true),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Future<bool> _onWillPop() async {
    if (!_hasUnsavedChanges) return true;

    final result = await showDialog<String>(
      context: context,
      barrierDismissible: false,
      builder: (context) => UnsavedChangesPopup(
        onCancel: () => Navigator.of(context).pop('cancel'),
        onSave: () => Navigator.of(context).pop('save'),
        onExit: () => Navigator.of(context).pop('exit'),
      ),
    );

    if (result == 'save') {
      await _saveStructure();
      return false; // Già gestito in _saveStructure
    } else if (result == 'exit') {
      return true;
    }
    return false; // cancel
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return PopScope(
      canPop: !_hasUnsavedChanges,
      onPopInvokedWithResult: (didPop, result) async {
        if (!didPop) {
          final shouldPop = await _onWillPop();
          if (shouldPop && context.mounted) {
            Navigator.of(context).pop();
          }
        }
      },
      child: Scaffold(
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
                      onPressed: () async {
                        final shouldPop = await _onWillPop();
                        if (shouldPop && mounted) {
                          Navigator.of(context).pop();
                        }
                      },
                    ),
                    const Spacer(),
                    Text(
                      'STRUTTURA CUSTOM',
                      style: theme.textTheme.headlineSmall,
                    ),
                    const Spacer(),
                    const SizedBox(width: 48),
                  ],
                ),
              ),
              // Lista livelli o messaggio vuoto
              Expanded(
                child: _levels.isEmpty
                    ? Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text(
                              'Nessun livello',
                              style: theme.textTheme.headlineMedium?.copyWith(
                                color: theme.colorScheme.onSurface.withOpacity(0.5),
                              ),
                            ),
                            const SizedBox(height: 16),
                            Text(
                              'Premi OK per aggiungere\nil primo livello',
                              style: theme.textTheme.bodyLarge?.copyWith(
                                color: theme.colorScheme.onSurface.withOpacity(0.5),
                              ),
                              textAlign: TextAlign.center,
                            ),
                            const SizedBox(height: 48),
                            FocusableButton(
                              text: '+ AGGIUNGI LV.1',
                              autofocus: true,
                              onPressed: _addLevel,
                            ),
                          ],
                        ),
                      )
                    : ListView(
                        controller: _scrollController,
                        padding: const EdgeInsets.symmetric(horizontal: 32),
                        children: [
                          // Lista livelli
                          for (int i = 0; i < _levels.length; i++)
                            Padding(
                              padding: const EdgeInsets.only(bottom: 8),
                              child: LevelRow(
                                level: _levels[i],
                                hasFocus: false,
                                onEditSmallBlind: () => _editSmallBlind(i),
                                onEditDuration: () => _editDuration(i),
                                onDelete: () => _deleteLevel(i),
                              ),
                            ),
                          const SizedBox(height: 16),
                          // Pulsante aggiungi
                          Center(
                            child: FocusableButton(
                              text: '+ AGGIUNGI LV.${_levels.length + 1}',
                              onPressed: _addLevel,
                            ),
                          ),
                          const SizedBox(height: 32),
                          // Pulsanti SALVA e RESET
                          Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              FocusableButton(
                                text: 'SALVA',
                                onPressed: _saveStructure,
                              ),
                              const SizedBox(width: 16),
                              FocusableButton(
                                text: 'RESET',
                                customBackgroundColor: Colors.red,
                                onPressed: _resetStructure,
                              ),
                            ],
                          ),
                          const SizedBox(height: 32),
                        ],
                      ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
