package com.dermatoai.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dermatoai.R
import com.dermatoai.databinding.FragmentSignInBinding
import com.dermatoai.oauth.GoogleAuthenticationService
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding

    @Inject
    lateinit var googleSignInService: GoogleAuthenticationService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiBind(view)
    }

    private fun uiBind(view: View) {
        binding.googleSignInButton.setOnClickListener {
            val request = getCredentialRequest()
            viewLifecycleOwner.lifecycleScope.launch {
                googleSignInService.doSignIn(
                    requireContext(),
                    request,
                    {
                        requireActivity().startActivity(
                            Intent(
                                requireContext(),
                                BaseActivity::class.java
                            )
                        )
                        requireActivity().finish()
                    }, { error ->
                        Log.e("SIGN IN", error.message.toString())
                        if (error is GetCredentialException) {
                            Snackbar.make(
                                view,
                                "Credential cannot found by app",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                            return@doSignIn
                        }
                        Snackbar.make(view, error.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                    }
                )
            }
        }

        binding.signUpButton.setOnClickListener {
            val request = getCredentialRequest()
            viewLifecycleOwner.lifecycleScope.launch {
                googleSignInService.doSignUp(
                    requireContext(),
                    request,
                    { userId ->

                        lifecycleScope.launch {
                            withContext(Dispatchers.Main) {
                                findNavController().navigate(
                                    SignInFragmentDirections.actionSignInFragmentToBirthFragment(
                                        userId
                                    )
                                )
                            }
                        }
                    }, { error ->
                        Log.e("SIGN IN", error.message.toString())
                        if (error is GetCredentialException) {
                            Snackbar.make(
                                view,
                                "Credential cannot found by app",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                            return@doSignUp
                        }
                        Snackbar.make(view, error.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                    }
                )
            }
        }
    }

    private fun getCredentialRequest(): GetCredentialRequest {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, byte ->
            str + "%02x".format(byte)
        }

        val signInWithGoogleOption: GetSignInWithGoogleOption =
            GetSignInWithGoogleOption.Builder(binding.root.context.getString(R.string.default_web_client_id))
                .setNonce(hashedNonce)
                .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()
        return request
    }
}