FROM libretranslate/libretranslate:latest

RUN /app/venv/bin/pip install --upgrade \
    "urllib3<2" \
    "charset_normalizer<3"