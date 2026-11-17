package com.quizscan.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.quizscan.app.R
import com.quizscan.app.databinding.FragmentAuthBinding
import kotlinx.coroutines.launch

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.isUserSignedIn()) {
            navigateToHome()
            return
        }

        binding.btnGoogleSignIn.setOnClickListener {
            launchCredentialManager()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AuthUiState.Idle -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnGoogleSignIn.isEnabled = true
                        }
                        is AuthUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnGoogleSignIn.isEnabled = false
                        }
                        is AuthUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            navigateToHome()
                        }
                        is AuthUiState.Cancelled -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnGoogleSignIn.isEnabled = true
                            Toast.makeText(requireContext(), R.string.error_sign_in_cancelled, Toast.LENGTH_SHORT).show()
                            viewModel.resetState()
                        }
                        is AuthUiState.NoNetwork -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnGoogleSignIn.isEnabled = true
                            Toast.makeText(requireContext(), R.string.error_no_network, Toast.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                        is AuthUiState.Failure -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnGoogleSignIn.isEnabled = true
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                    }
                }
            }
        }
    }

    private fun launchCredentialManager() {
        val serverClientId = getString(R.string.default_web_client_id)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = credentialManager.getCredential(
                    request = request,
                    context = requireActivity()
                )

                val credential = response.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    viewModel.signInWithCredential(authCredential)
                } else {
                    viewModel.handleSignInFailure(IllegalStateException("Unexpected credential type"))
                }
            } catch (e: GetCredentialCancellationException) {
                viewModel.handleSignInCancelled()
            } catch (e: Exception) {
                viewModel.handleSignInFailure(e)
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_authFragment_to_homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
