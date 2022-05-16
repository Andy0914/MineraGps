package urias.andres.minera_gps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_reportar_material.*

class ReportarMaterialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportar_material)
        val bundle = intent.extras
        val dato = bundle?.getString("matricula")
        editTextMatricula.setText(dato)
        atras.setOnClickListener {
            finish()
        }

        reportar.setOnClickListener {
            finish()
        }

    }
}