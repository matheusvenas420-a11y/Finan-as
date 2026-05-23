from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import (
    SimpleDocTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
    PageBreak,
)


OUTPUT = "Explicacao_Projeto_Fintrack.pdf"
GREEN = colors.HexColor("#10B981")
TEXT = colors.HexColor("#111827")
MUTED = colors.HexColor("#6B7280")
LIGHT = colors.HexColor("#F3F4F6")


def build_styles():
    styles = getSampleStyleSheet()
    styles.add(
        ParagraphStyle(
            name="CoverTitle",
            parent=styles["Title"],
            fontName="Helvetica-Bold",
            fontSize=30,
            leading=36,
            textColor=TEXT,
            spaceAfter=12,
        )
    )
    styles.add(
        ParagraphStyle(
            name="CoverSubtitle",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=12,
            leading=18,
            textColor=MUTED,
            alignment=1,
        )
    )
    styles.add(
        ParagraphStyle(
            name="SectionTitle",
            parent=styles["Heading1"],
            fontName="Helvetica-Bold",
            fontSize=17,
            leading=22,
            textColor=TEXT,
            spaceBefore=14,
            spaceAfter=8,
        )
    )
    styles.add(
        ParagraphStyle(
            name="SubTitle",
            parent=styles["Heading2"],
            fontName="Helvetica-Bold",
            fontSize=12,
            leading=16,
            textColor=TEXT,
            spaceBefore=10,
            spaceAfter=5,
        )
    )
    styles.add(
        ParagraphStyle(
            name="Body",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=10,
            leading=15,
            textColor=TEXT,
            spaceAfter=6,
        )
    )
    styles.add(
        ParagraphStyle(
            name="Small",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=8.8,
            leading=12,
            textColor=MUTED,
        )
    )
    return styles


def p(text, style):
    return Paragraph(text, style)


def bullet(text, styles):
    return p(f"&#8226; {text}", styles["Body"])


def make_table(data, widths):
    table = Table(data, colWidths=widths, hAlign="LEFT")
    table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), GREEN),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
                ("FONTSIZE", (0, 0), (-1, -1), 8.5),
                ("LEADING", (0, 0), (-1, -1), 11),
                ("TEXTCOLOR", (0, 1), (-1, -1), TEXT),
                ("BACKGROUND", (0, 1), (-1, -1), colors.white),
                ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#D1D5DB")),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("LEFTPADDING", (0, 0), (-1, -1), 7),
                ("RIGHTPADDING", (0, 0), (-1, -1), 7),
                ("TOPPADDING", (0, 0), (-1, -1), 6),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
            ]
        )
    )
    return table


def add_footer(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(MUTED)
    canvas.drawString(2 * cm, 1.2 * cm, "Fintrack - Explicacao do projeto")
    canvas.drawRightString(A4[0] - 2 * cm, 1.2 * cm, f"Pagina {doc.page}")
    canvas.restoreState()


def build_pdf():
    styles = build_styles()
    doc = SimpleDocTemplate(
        OUTPUT,
        pagesize=A4,
        rightMargin=2 * cm,
        leftMargin=2 * cm,
        topMargin=2 * cm,
        bottomMargin=2 * cm,
        title="Explicacao do Projeto Fintrack",
    )

    story = []

    cover_logo = Table(
        [["Fintrack"]],
        colWidths=[8 * cm],
        rowHeights=[1.6 * cm],
        hAlign="CENTER",
    )
    cover_logo.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, -1), GREEN),
                ("TEXTCOLOR", (0, 0), (-1, -1), colors.white),
                ("FONTNAME", (0, 0), (-1, -1), "Helvetica-Bold"),
                ("FONTSIZE", (0, 0), (-1, -1), 24),
                ("ALIGN", (0, 0), (-1, -1), "CENTER"),
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                ("BOX", (0, 0), (-1, -1), 0, GREEN),
            ]
        )
    )

    story.append(Spacer(1, 4 * cm))
    story.append(cover_logo)
    story.append(Spacer(1, 0.8 * cm))
    story.append(p("Explicacao do Projeto Fintrack", styles["CoverTitle"]))
    story.append(
        p(
            "Aplicativo Android em Java para controle financeiro mensal, com Firebase Authentication e Cloud Firestore.",
            styles["CoverSubtitle"],
        )
    )
    story.append(Spacer(1, 1.2 * cm))
    story.append(
        p(
            "Documento gerado para resumir a arquitetura, telas, modelos de dados e principais fluxos implementados.",
            styles["CoverSubtitle"],
        )
    )
    story.append(PageBreak())

    story.append(p("1. Visao Geral", styles["SectionTitle"]))
    story.append(
        p(
            "O Fintrack e um aplicativo Android desenvolvido em Java para registrar salario mensal, gastos por categoria, formas de pagamento e saldo disponivel. O app usa Firebase Authentication para login/cadastro e Cloud Firestore para persistir os dados do usuario.",
            styles["Body"],
        )
    )
    story.append(bullet("Cada usuario possui seus proprios documentos dentro de usuarios/{uid}.", styles))
    story.append(bullet("Os gastos e salarios sao separados por mes e ano usando o campo mesAno.", styles))
    story.append(bullet("A tela principal atualiza automaticamente os totais a partir dos listeners do Firestore.", styles))
    story.append(bullet("Meses anteriores continuam salvos para consulta historica futura.", styles))

    story.append(p("2. Principais Telas", styles["SectionTitle"]))
    table_data = [
        ["Tela", "Arquivo", "Responsabilidade"],
        ["Splash", "SplashActivity / activity_splash.xml", "Exibe a logo e o nome do app antes do login."],
        ["Login", "MainActivity / activity_main.xml", "Autentica o usuario com Firebase Authentication."],
        ["Cadastro", "CadastroActivity / activity_cadastrar.xml", "Cria conta e salva dados basicos do usuario."],
        ["Principal", "PrincipalActivity / activity_principal.xml", "Mostra resumo mensal, categorias, saldo disponivel e atalhos."],
        ["Adicionar gasto", "AdicionarGastoActivity / activity_adicionar_gasto.xml", "Registra gasto com categoria, pagamento e parcelas."],
        ["Adicionar salario", "AdicionarSalarioActivity / activity_adicionar_salario.xml", "Salva o salario do mes atual."],
        ["Gastos por categoria", "GastosCategoriaActivity / activity_gastos_categoria.xml", "Lista gastos de uma categoria e permite deletar."],
    ]
    story.append(make_table(table_data, [3.4 * cm, 5.2 * cm, 7.2 * cm]))

    story.append(p("3. Fluxo de Autenticacao", styles["SectionTitle"]))
    story.append(
        p(
            "O usuario inicia na SplashActivity e depois e direcionado para a MainActivity. A tela de login usa FirebaseAuth.signInWithEmailAndPassword. Quando o login e bem-sucedido, o app abre a PrincipalActivity.",
            styles["Body"],
        )
    )
    story.append(
        p(
            "No cadastro, CadastroActivity cria o usuario com FirebaseAuth.createUserWithEmailAndPassword e salva um documento Usuario em usuarios/{uid}.",
            styles["Body"],
        )
    )

    story.append(p("4. Organizacao no Firestore", styles["SectionTitle"]))
    firestore_data = [
        ["Colecao / Documento", "Conteudo"],
        ["usuarios/{uid}", "Dados basicos do usuario, como nome e email."],
        ["usuarios/{uid}/gastos/{id}", "Cada gasto individual, com valor, categoria, data, forma de pagamento, parcelas e mesAno."],
        ["usuarios/{uid}/salarios/{mesAno}", "Salario registrado para o mes correspondente."],
        ["usuarios/{uid}/resumosMensais/{mesAno}", "Resumo calculado: salario, total de gastos e saldo restante."],
    ]
    story.append(make_table(firestore_data, [6.5 * cm, 9.3 * cm]))

    story.append(p("5. Models", styles["SectionTitle"]))
    model_data = [
        ["Classe", "Campos principais", "Uso"],
        ["Usuario", "nome, email", "Representa o documento principal do usuario."],
        ["Gasto", "descricao, valor, categoria, formaPagamento, parcelas, data, mes, ano, mesAno", "Representa cada gasto salvo no Firestore."],
        ["Salario", "valor, data, mes, ano, mesAno", "Representa o salario mensal do usuario."],
        ["ResumoMensal", "salario, totalGastos, saldoRestante, mes, ano, mesAno, atualizadoEm", "Guarda o historico mensal calculado."],
    ]
    story.append(make_table(model_data, [3.4 * cm, 8 * cm, 4.4 * cm]))

    story.append(p("6. Fluxo de Gastos", styles["SectionTitle"]))
    story.append(
        p(
            "Ao adicionar um gasto, o usuario informa descricao, valor, categoria, forma de pagamento e, se a forma for Credito, a quantidade de parcelas. O valor usa mascara automatica em reais, convertendo a digitacao para formato monetario.",
            styles["Body"],
        )
    )
    story.append(bullet("O gasto e salvo em usuarios/{uid}/gastos.", styles))
    story.append(bullet("O campo mesAno permite separar gastos por mes sem apagar registros antigos.", styles))
    story.append(bullet("A tela principal escuta alteracoes e recalcula os totais automaticamente.", styles))
    story.append(bullet("Na listagem por categoria, o usuario pode tocar em um gasto e deletar o documento.", styles))

    story.append(p("7. Salario, Saldo e Historico Mensal", styles["SectionTitle"]))
    story.append(
        p(
            "O salario do mes e salvo no documento usuarios/{uid}/salarios/{mesAno}. A PrincipalActivity calcula o saldo disponivel subtraindo os gastos do mes atual do salario registrado.",
            styles["Body"],
        )
    )
    story.append(bullet("Se nao houver salario no mes atual, o salario considerado e zero.", styles))
    story.append(bullet("Se nao houver gastos no mes atual, os blocos de gastos ficam ocultos e o saldo usa apenas o salario.", styles))
    story.append(bullet("Se os gastos ultrapassarem o salario, o saldo fica negativo e aparece em vermelho.", styles))
    story.append(bullet("O resumo mensal e salvo em resumosMensais/{mesAno}, preservando historico financeiro.", styles))

    story.append(p("8. Atualizacao Automatica", styles["SectionTitle"]))
    story.append(
        p(
            "A tela principal usa addSnapshotListener para observar salarios e gastos. Isso significa que qualquer criacao, alteracao ou exclusao no Firestore dispara a atualizacao da interface em tempo real.",
            styles["Body"],
        )
    )
    story.append(bullet("Ao adicionar gasto: total de gastos, categorias e saldo sao recalculados.", styles))
    story.append(bullet("Ao deletar gasto: o documento some da lista e os totais diminuem automaticamente.", styles))
    story.append(bullet("Ao adicionar salario: o saldo disponivel e atualizado sem precisar reiniciar o app.", styles))

    story.append(p("9. Padrao Visual", styles["SectionTitle"]))
    story.append(
        p(
            "As telas seguem um padrao visual consistente: fundo claro, cards brancos, botoes verdes com a cor da marca, tipografia simples e icones vetoriais para categorias e a logo.",
            styles["Body"],
        )
    )
    story.append(bullet("Cor principal: #10B981.", styles))
    story.append(bullet("Logo: carteira verde em fundo branco.", styles))
    story.append(bullet("Botoes principais: backgroundTint @color/emerald_green.", styles))

    story.append(p("10. Pontos para Evolucao", styles["SectionTitle"]))
    story.append(bullet("Criar tela de historico para consultar meses anteriores.", styles))
    story.append(bullet("Permitir editar gastos e salario ja cadastrados.", styles))
    story.append(bullet("Adicionar filtro por forma de pagamento.", styles))
    story.append(bullet("Gerar relatorios mensais em PDF diretamente pelo app.", styles))
    story.append(bullet("Adicionar graficos para visualizar categorias ao longo do tempo.", styles))

    doc.build(story, onFirstPage=add_footer, onLaterPages=add_footer)


if __name__ == "__main__":
    build_pdf()
