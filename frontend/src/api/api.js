/**
 * Legacy API file - kept for backward compatibility
 * New code should use the modular API structure in:
 * - api/client.js (base client)
 * - api/auth.js, api/datasets.js, api/models.js, etc. (endpoint modules)
 * 
 * This file re-exports from the new structure for compatibility
 */
export { authAPI, datasetsAPI, modelsAPI, predictionsAPI, dashboardAPI } from './index';
export { default as api } from './client';

// Legacy exports for backward compatibility
import { authAPI as newAuthAPI, datasetsAPI as newDatasetsAPI, modelsAPI as newModelsAPI } from './index';

export const authAPI = newAuthAPI;
export const datasetAPI = newDatasetsAPI;
export const modelAPI = newModelsAPI;
