import env from '../app/env-prod.json';
import {HelperService} from "../app/helpers/util";

export const environment = {
  production: true,
  get: function (key: any) {
    return HelperService.getEnvVar(key, env);
  }
};
