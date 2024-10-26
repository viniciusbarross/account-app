package br.edu.utfpr.trabalhofinal.ui.conta.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.utfpr.trabalhofinal.R
import br.edu.utfpr.trabalhofinal.ui.theme.TrabalhoFinalTheme
import br.edu.utfpr.trabalhofinal.ui.utils.composables.Carregando
import br.edu.utfpr.trabalhofinal.ui.utils.composables.ErroAoCarregar
import java.util.*

@Composable
fun FormularioContaScreen(
    modifier: Modifier = Modifier,
    onVoltarPressed: () -> Unit,
    viewModel: FormularioContaViewModel = viewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    LaunchedEffect(viewModel.state.contaPersistidaOuRemovida) {
        if (viewModel.state.contaPersistidaOuRemovida) {
            onVoltarPressed()
        }
    }
    val context = LocalContext.current
    LaunchedEffect(snackbarHostState, viewModel.state.codigoMensagem) {
        viewModel.state.codigoMensagem
            .takeIf { it > 0 }
            ?.let {
                snackbarHostState.showSnackbar(context.getString(it))
                viewModel.onMensagemExibida()
            }
    }

    if (viewModel.state.mostrarDialogConfirmacao) {
        ConfirmationDialog(
            title = stringResource(R.string.atencao),
            text = stringResource(R.string.mensagem_confirmacao_remover_contato),
            onDismiss = viewModel::ocultarDialogConfirmacao,
            onConfirm = viewModel::removerConta
        )
    }

    val contentModifier: Modifier = modifier.fillMaxSize()
    if (viewModel.state.carregando) {
        Carregando(modifier = contentModifier)
    } else if (viewModel.state.erroAoCarregar) {
        ErroAoCarregar(
            modifier = contentModifier,
            onTryAgainPressed = viewModel::carregarConta
        )
    } else {
        Scaffold(
            modifier = contentModifier,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                AppBar(
                    contaNova = viewModel.state.contaNova,
                    processando = viewModel.state.salvando || viewModel.state.excluindo,
                    onVoltarPressed = onVoltarPressed,
                    onSalvarPressed = viewModel::salvarConta,
                    onExcluirPressed = viewModel::mostrarDialogConfirmacao
                )
            }
        ) { paddingValues ->
            FormContent(
                modifier = Modifier.padding(paddingValues),
                processando = viewModel.state.salvando || viewModel.state.excluindo,
                descricao = viewModel.state.descricao,
                data = viewModel.state.data,
                valor = viewModel.state.valor,
                paga = viewModel.state.paga,
                tipo = viewModel.state.tipo,
                onDescricaoAlterada = viewModel::onDescricaoAlterada,
                onDataAlterada = viewModel::onDataAlterada,
                onValorAlterado = viewModel::onValorAlterado,
                onStatusPagamentoAlterado = viewModel::onStatusPagamentoAlterado,
                onTipoAlterado = viewModel::onTipoAlterado
            )
        }
    }
}

@Composable
fun ConfirmationDialog(
    modifier: Modifier = Modifier,
    title: String? = null,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    dismissButtonText: String? = null,
    confirmButtonText: String? = null
) {
    AlertDialog(
        modifier = modifier,
        title = title?.let { { Text(it) } },
        text = { Text(text) },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmButtonText ?: stringResource(R.string.confirmar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissButtonText ?: stringResource(R.string.cancelar))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
    modifier: Modifier = Modifier,
    contaNova: Boolean,
    processando: Boolean,
    onVoltarPressed: () -> Unit,
    onSalvarPressed: () -> Unit,
    onExcluirPressed: () -> Unit
) {
    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        title = {
            Text(if (contaNova) {
                stringResource(R.string.nova_conta)
            } else {
                stringResource(R.string.editar_conta)
            })
        },
        navigationIcon = {
            IconButton(onClick = onVoltarPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.voltar)
                )
            }
        },
        actions = {
            if (processando) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(all = 16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                if (!contaNova) {
                    IconButton(onClick = onExcluirPressed) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.excluir)
                        )
                    }
                }
                IconButton(onClick = onSalvarPressed) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.salvar)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun FormContent(
    modifier: Modifier = Modifier,
    processando: Boolean,
    descricao: CampoFormulario,
    data: CampoFormulario,
    valor: CampoFormulario,
    paga: Boolean,
    tipo: CampoFormulario,
    onDescricaoAlterada: (String) -> Unit,
    onDataAlterada: (String) -> Unit,
    onValorAlterado: (String) -> Unit,
    onStatusPagamentoAlterado: (Boolean) -> Unit,
    onTipoAlterado: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Column(
        modifier = modifier
            .padding(all = 16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FormTextField(
            titulo = stringResource(R.string.descricao),
            campoFormulario = descricao,
            onValorAlterado = onDescricaoAlterada,
            enabled = !processando
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = stringResource(R.string.data),
                tint = MaterialTheme.colorScheme.outline
            )
            FormDatePicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                campoFormulario = data,
                onValorAlterado = onDataAlterada,
                calendar = calendar,
                context = context
            )
        }

        FormTextField(
            titulo = stringResource(R.string.valor),
            campoFormulario = valor,
            onValorAlterado = onValorAlterado,
            enabled = !processando
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            FormCheckbox(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                checked = paga,
                onCheckChanged = onStatusPagamentoAlterado,
                enabled = !processando,
                label = stringResource(R.string.paga)
            )

        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            RadioButton(
                selected = tipo.valor == "Receita",
                onClick = { onTipoAlterado("Receita") },
                enabled = !processando
            )
            Text(text = stringResource(R.string.receita))
            Spacer(modifier = Modifier.size(16.dp))
            RadioButton(
                selected = tipo.valor == "Despesa",
                onClick = { onTipoAlterado("Despesa") },
                enabled = !processando
            )
            Text(text = stringResource(R.string.despesa))
        }
    }
}

@Composable
fun FormTextField(
    titulo: String,
    campoFormulario: CampoFormulario,
    onValorAlterado: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = campoFormulario.valor,
            onValueChange = onValorAlterado,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = enabled,
            isError = campoFormulario.contemErro,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation
        )
        if (campoFormulario.contemErro) {
            Text(
                text = stringResource(id = campoFormulario.codigoMensagemErro),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
fun FormDatePicker(
    modifier: Modifier = Modifier,
    campoFormulario: CampoFormulario,
    onValorAlterado: (String) -> Unit,
    calendar: Calendar,
    context: android.content.Context
) {

    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->

                val selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                onValorAlterado(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    OutlinedTextField(
        value = campoFormulario.valor,
        onValueChange = {},
        modifier = modifier,
        readOnly = true,
        enabled = !campoFormulario.contemErro,
        label = { Text(text = stringResource(R.string.data)) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = stringResource(R.string.selecionar_data),
                modifier = Modifier.clickable { datePickerDialog.show() }
            )
        },
        isError = campoFormulario.contemErro
    )
}


@Composable
fun FormCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckChanged: (Boolean) -> Unit,
    enabled: Boolean= true,
    label: String
) {
    Row (
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ){
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckChanged,
            enabled = enabled
        )
        Text(label)

    }

}