from argostranslate import package, translate
import sys

installed_languages = translate.get_installed_languages()

from_lang = next((lang for lang in installed_languages if lang.code == "en"), None)
to_lang = next((lang for lang in installed_languages if lang.code == "pt"), None)

if from_lang is None or to_lang is None:
    print("ERRO: idioma não instalado", file=sys.stderr)
    sys.exit(1)

translation = from_lang.get_translation(to_lang)


def traduzir_linha(linha):
    indent = len(linha) - len(linha.lstrip())
    espacos = linha[:indent]

    conteudo = linha.strip()

    if not conteudo:
        return ""

    try:
        traduzido = translation.translate(conteudo)
    except Exception as e:
        print(f"ERRO: {e}", file=sys.stderr)
        return linha

    return espacos + traduzido


def traduzir_texto(texto):
    linhas = texto.split("\n")
    resultado = []

    for linha in linhas:
        resultado.append(traduzir_linha(linha))

    return "\n".join(resultado)


if __name__ == "__main__":
    texto = sys.stdin.read()
    print(traduzir_texto(texto))