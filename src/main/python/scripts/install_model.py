import argostranslate.package
import argostranslate.translate

argostranslate.package.update_package_index()

available_packages = argostranslate.package.get_available_packages()

package_to_install = next(
    filter(
        lambda x: x.from_code == "en" and x.to_code == "pt",
        available_packages
    )
)

argostranslate.package.install_from_path(package_to_install.download())

print("Modelo instalado!")